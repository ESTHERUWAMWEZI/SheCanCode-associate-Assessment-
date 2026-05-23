package rw.igirepay.gateway.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rw.igirepay.gateway.config.IdempotencyProperties;
import rw.igirepay.gateway.dto.IdempotencyResult;
import rw.igirepay.gateway.dto.PaymentRequest;
import rw.igirepay.gateway.dto.PaymentResponse;
import rw.igirepay.gateway.exception.ConflictingIdempotencyKeyException;
import rw.igirepay.gateway.exception.PaymentProcessingException;
import rw.igirepay.gateway.model.AuditEvent;
import rw.igirepay.gateway.model.AuditEventType;
import rw.igirepay.gateway.model.IdempotencyRecord;
import rw.igirepay.gateway.model.IdempotencyStatus;
import rw.igirepay.gateway.service.AuditService;
import rw.igirepay.gateway.service.IdempotencyService;
import rw.igirepay.gateway.service.PaymentService;
import rw.igirepay.gateway.store.IdempotencyStore;
import rw.igirepay.gateway.util.HashUtil;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Core idempotency engine for IgirePay gateway.
 *
 * Concurrency design:
 *   The atomicity guarantee comes from ConcurrentHashMap.putIfAbsent(). At most
 *   one thread can "win" the insertion for a given key. The winner processes the
 *   payment and completes the CompletableFuture stored in the record. All other
 *   threads that arrive for the same key — whether the record is PROCESSING or
 *   COMPLETED — wait on or join that future. This means:
 *
 *   - No duplicate payments: only the winner calls PaymentService.
 *   - No busy-waiting: losers block efficiently on CompletableFuture.get().
 *   - Correct memory visibility: the future's completion establishes a
 *     happens-before relationship, so the response written by the winner is
 *     safely visible to all waiting threads when the future resolves.
 *
 * Three scenarios handled:
 *   1. First request          → process, cache, audit, return (cacheHit=false)
 *   2. Retry (same payload)   → return cached future result, audit (cacheHit=true)
 *   3. Conflict (diff payload)→ audit + throw ConflictingIdempotencyKeyException → 409
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final int CONCURRENT_WAIT_TIMEOUT_SECONDS = 35;

    private final IdempotencyStore store;
    private final PaymentService paymentService;
    private final HashUtil hashUtil;
    private final IdempotencyProperties properties;
    private final AuditService auditService;

    @Override
    public IdempotencyResult processWithIdempotency(String idempotencyKey, PaymentRequest request) {
        String requestHash = hashUtil.sha256(request);
        LocalDateTime now = LocalDateTime.now();

        IdempotencyRecord candidate = buildNewRecord(idempotencyKey, requestHash, now);

        // Atomic: returns null if inserted (we are first), or the existing record
        IdempotencyRecord existing = store.putIfAbsent(idempotencyKey, candidate);

        if (existing == null) {
            return processFirstRequest(candidate, request, idempotencyKey, requestHash);
        }

        return handleExistingRecord(existing, requestHash, idempotencyKey, request);
    }

    // -------------------------------------------------------------------------
    // First-request path
    // -------------------------------------------------------------------------

    private IdempotencyResult processFirstRequest(
            IdempotencyRecord record, PaymentRequest request,
            String idempotencyKey, String requestHash) {

        log.info("First request for key={} — delegating to payment processor", idempotencyKey);

        try {
            PaymentResponse response = paymentService.processPayment(request, idempotencyKey);

            // Write response fields before completing the future.
            // Threads waiting on the future will see these writes due to the
            // happens-before guarantee established by CompletableFuture.complete().
            record.setCachedResponse(response);
            record.setHttpStatusCode(200);
            record.setStatus(IdempotencyStatus.COMPLETED);
            record.getProcessingFuture().complete(response);

            audit(AuditEventType.PAYMENT_PROCESSED, idempotencyKey, request, requestHash,
                    "Payment processed successfully. transactionId=" + response.getTransactionId());

            log.info("Payment cached for key={}", idempotencyKey);
            return new IdempotencyResult(response, false, 200);

        } catch (Exception e) {
            // Remove the failed record so the client can safely retry with the same key.
            // Complete exceptionally so any concurrent waiters unblock with an error
            // instead of waiting out the full timeout.
            record.getProcessingFuture().completeExceptionally(e);
            store.remove(idempotencyKey);

            audit(AuditEventType.PAYMENT_FAILED, idempotencyKey, request, requestHash,
                    "Payment failed: " + e.getMessage());

            log.error("Payment failed for key={}: {}", idempotencyKey, e.getMessage());
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Duplicate / concurrent request path
    // -------------------------------------------------------------------------

    private IdempotencyResult handleExistingRecord(
            IdempotencyRecord existing, String requestHash,
            String idempotencyKey, PaymentRequest request) {

        // USER STORY 3: same key, different payload → 409 Conflict
        if (!existing.getRequestBodyHash().equals(requestHash)) {
            log.warn("Payload conflict detected for key={}", idempotencyKey);
            audit(AuditEventType.PAYLOAD_CONFLICT, idempotencyKey, request, requestHash,
                    "Rejected: same key used with a different request body.");
            throw new ConflictingIdempotencyKeyException(
                    "Idempotency key already used for a different request body.");
        }

        // USER STORY 2 + CONCURRENCY: same key, same payload.
        // Always resolve through the CompletableFuture — never read cachedResponse
        // directly. This guarantees correct memory visibility regardless of whether
        // the record is in PROCESSING or COMPLETED state at this moment.
        log.info("Duplicate/concurrent request for key={} — waiting on processing future", idempotencyKey);

        try {
            PaymentResponse response = existing.getProcessingFuture()
                    .get(CONCURRENT_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            audit(AuditEventType.CACHE_HIT, idempotencyKey, request, requestHash,
                    "Duplicate request served from cache. No charge applied.");

            log.info("Returning cached response for key={}", idempotencyKey);
            return new IdempotencyResult(response, true, existing.getHttpStatusCode());

        } catch (TimeoutException e) {
            throw new PaymentProcessingException(
                    "Timed out waiting for the in-flight payment to complete. Please retry.");
        } catch (ExecutionException e) {
            throw new PaymentProcessingException(
                    "The original payment request failed: " + e.getCause().getMessage(),
                    e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessingException(
                    "Request interrupted while waiting for payment completion.");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private IdempotencyRecord buildNewRecord(String key, String hash, LocalDateTime now) {
        return IdempotencyRecord.builder()
                .idempotencyKey(key)
                .requestBodyHash(hash)
                .status(IdempotencyStatus.PROCESSING)
                .processingFuture(new CompletableFuture<>())
                .createdAt(now)
                .expiresAt(now.plusMinutes(properties.getTtlMinutes()))
                .httpStatusCode(0)
                .build();
    }

    private void audit(AuditEventType type, String key, PaymentRequest request,
                       String hash, String description) {
        auditService.record(AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .idempotencyKey(key)
                .eventType(type)
                .amount(request != null ? request.getAmount().toPlainString() : null)
                .currency(request != null ? request.getCurrency() : null)
                .requestBodyHash(hash)
                .description(description)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
