package rw.igirepay.gateway.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.igirepay.gateway.dto.IdempotencyResult;
import rw.igirepay.gateway.dto.PaymentRequest;
import rw.igirepay.gateway.dto.PaymentResponse;
import rw.igirepay.gateway.exception.MissingIdempotencyKeyException;
import rw.igirepay.gateway.model.AuditEvent;
import rw.igirepay.gateway.model.AuditEventType;
import rw.igirepay.gateway.service.AuditService;
import rw.igirepay.gateway.service.IdempotencyService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST entry point for the IgirePay payment gateway.
 *
 * This controller is intentionally thin — it validates the presence of the
 * required header, delegates all business logic to IdempotencyService, and
 * maps the result to an HTTP response. No payment or idempotency logic lives here.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String CACHE_HIT_HEADER       = "X-Cache-Hit";

    private final IdempotencyService idempotencyService;
    private final AuditService auditService;

    /**
     * POST /api/v1/process-payment
     *
     * Required header: Idempotency-Key: <unique-string>
     * Body: { "amount": 100, "currency": "RWF" }
     *
     * Returns:
     *   200 with X-Cache-Hit: true  → duplicate request served from cache
     *   200 without X-Cache-Hit     → first request, payment processed
     *   400                         → missing Idempotency-Key header
     *   409                         → same key used with different payload
     *   422                         → request body validation failure
     *   500                         → payment processing error
     */
    @PostMapping("/process-payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            auditService.record(AuditEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(AuditEventType.MISSING_IDEMPOTENCY_KEY)
                    .amount(request.getAmount().toPlainString())
                    .currency(request.getCurrency())
                    .description("Request rejected: Idempotency-Key header was absent.")
                    .timestamp(LocalDateTime.now())
                    .build());

            throw new MissingIdempotencyKeyException(
                    "The '" + IDEMPOTENCY_KEY_HEADER + "' request header is required.");
        }

        log.info("Received payment request — key={}, amount={} {}",
                idempotencyKey, request.getAmount(), request.getCurrency());

        IdempotencyResult result = idempotencyService.processWithIdempotency(idempotencyKey, request);

        HttpHeaders responseHeaders = new HttpHeaders();
        if (result.isCacheHit()) {
            responseHeaders.add(CACHE_HIT_HEADER, "true");
        }

        return ResponseEntity
                .status(result.getHttpStatusCode())
                .headers(responseHeaders)
                .body(result.getResponse());
    }
}
