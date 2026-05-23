package rw.igirepay.gateway.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String CACHE_HIT_HEADER = "X-Cache-Hit";

    private final IdempotencyService idempotencyService;
    private final AuditService auditService;

    public PaymentController(IdempotencyService idempotencyService, AuditService auditService) {
        this.idempotencyService = idempotencyService;
        this.auditService = auditService;
    }

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

        log.info("Payment request received — key={}, amount={} {}",
                idempotencyKey, request.getAmount(), request.getCurrency());

        IdempotencyResult result = idempotencyService.processWithIdempotency(idempotencyKey, request);

        HttpHeaders headers = new HttpHeaders();
        if (result.isCacheHit()) {
            headers.add(CACHE_HIT_HEADER, "true");
        }

        return ResponseEntity.status(result.getHttpStatusCode()).headers(headers).body(result.getResponse());
    }
}
