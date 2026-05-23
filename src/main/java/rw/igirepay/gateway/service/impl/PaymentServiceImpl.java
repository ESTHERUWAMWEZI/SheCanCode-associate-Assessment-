package rw.igirepay.gateway.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rw.igirepay.gateway.config.PaymentProperties;
import rw.igirepay.gateway.dto.PaymentRequest;
import rw.igirepay.gateway.dto.PaymentResponse;
import rw.igirepay.gateway.exception.PaymentProcessingException;
import rw.igirepay.gateway.service.PaymentService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Simulated payment processor.
 *
 * In production, this class would delegate to an external payment provider
 * (MTN MoMo, Stripe, etc.) via HTTP or SDK. The 2-second sleep simulates
 * real-world network and processing latency, making concurrency tests
 * observable without mocking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentProperties paymentProperties;

    @Override
    public PaymentResponse processPayment(PaymentRequest request, String idempotencyKey) {
        log.info("Processing payment: key={}, amount={} {}",
                idempotencyKey, request.getAmount(), request.getCurrency());

        simulateProcessingDelay();

        String transactionId = UUID.randomUUID().toString().toUpperCase();

        log.info("Payment succeeded: transactionId={}, key={}", transactionId, idempotencyKey);

        return PaymentResponse.builder()
                .message("Charged " + request.getAmount().stripTrailingZeros().toPlainString()
                        + " " + request.getCurrency())
                .transactionId(transactionId)
                .idempotencyKey(idempotencyKey)
                .status("SUCCESS")
                .amount(request.getAmount().toPlainString())
                .currency(request.getCurrency())
                .processedAt(LocalDateTime.now())
                .build();
    }

    private void simulateProcessingDelay() {
        try {
            Thread.sleep(paymentProperties.getProcessingDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessingException("Payment processing was interrupted", e);
        }
    }
}
