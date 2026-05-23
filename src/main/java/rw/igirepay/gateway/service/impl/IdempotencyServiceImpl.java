package rw.igirepay.gateway.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyServiceImpl.class);
    private static final int CONCURRENT_WAIT_TIMEOUT_SECONDS = 35;

    private final IdempotencyStore store;
    private final PaymentService paymentService;
    private final HashUtil hashUtil;
    private final IdempotencyProperties properties;
    private final AuditService auditService;

    public IdempotencyServiceImpl(IdempotencyStore store, PaymentService paymentService,
                                   HashUtil hashUtil, IdempotencyProperties properties,
                                   AuditService auditService) {
        this.store = store;
        this.paymentService = paymentService;
        this.hashUtil = hashUtil;
        this.properties = properties;
        this.auditService = auditService;
    }

    @Override
    public IdempotencyResult processWithIdempotency(String idempotencyKey, PaymentRequest request) {
        String requestHash = hashUtil.sha256(request);
        LocalDateTime now = LocalDateTime.now();
        IdempotencyRecord candidate = buildNewRecord(idempotencyKey, requestHash, now);
        IdempotencyRecord existing = store.putIfAbsent(idempotencyKey, candidate);
        if (existing == null) {
            return processFirstRequest(candidate, request, idempotencyKey, requestHash);
        }
        return handleExistingRecord(existing, requestHash, idempotencyKey, request);
    }

    private IdempotencyResult processFirstRequest(IdempotencyRecord record, PaymentRequest request,
                                                   String idempotencyKey, String requestHash) {
        log.info("First request for key={}", idempotencyKey);
        try {
            PaymentResponse response = paymentService.processPayment(request, idempotencyKey);
            record.setCachedResponse(response);
            record.setHttpStatusCode(200);
            record.setStatus(IdempotencyStatus.COMPLETED);
            record.getProcessingFuture().complete(response);
            audit(AuditEventType.PAYMENT_PROCESSED, idempotencyKey, request, requestHash,
                    "Payment processed successfully. transactionId=" + response.getTransactionId());
            return new IdempotencyResult(response, false, 200);
        } catch (Exception e) {
            record.getProcessingFuture().completeExceptionally(e);
            store.remove(idempotencyKey);
            audit(AuditEventType.PAYMENT_FAILED, idempotencyKey, request, requestHash,
                    "Payment failed: " + e.getMessage());
            log.error("Payment failed for key={}: {}", idempotencyKey, e.getMessage());
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    private IdempotencyResult handleExistingRecord(IdempotencyRecord existing, String requestHash,
                                                    String idempotencyKey, PaymentRequest request) {
        if (!existing.getRequestBodyHash().equals(requestHash)) {
            log.warn("Payload conflict for key={}", idempotencyKey);
            audit(AuditEventType.PAYLOAD_CONFLICT, idempotencyKey, request, requestHash,
                    "Rejected: same key used with a different request body.");
            throw new ConflictingIdempotencyKeyException(
                    "Idempotency key already used for a different request body.");
        }
        log.info("Duplicate/concurrent request for key={} — awaiting future", idempotencyKey);
        try {
            PaymentResponse response = existing.getProcessingFuture()
                    .get(CONCURRENT_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            audit(AuditEventType.CACHE_HIT, idempotencyKey, request, requestHash,
                    "Duplicate request served from cache. No charge applied.");
            return new IdempotencyResult(response, true, existing.getHttpStatusCode());
        } catch (TimeoutException e) {
            throw new PaymentProcessingException(
                    "Timed out waiting for the in-flight payment to complete. Please retry.");
        } catch (ExecutionException e) {
            throw new PaymentProcessingException(
                    "The original payment request failed: " + e.getCause().getMessage(), e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessingException("Request interrupted while waiting for payment completion.");
        }
    }

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
