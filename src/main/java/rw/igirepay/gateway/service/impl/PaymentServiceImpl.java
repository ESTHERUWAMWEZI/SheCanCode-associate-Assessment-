package rw.igirepay.gateway.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rw.igirepay.gateway.config.PaymentProperties;
import rw.igirepay.gateway.dto.PaymentRequest;
import rw.igirepay.gateway.dto.PaymentResponse;
import rw.igirepay.gateway.exception.PaymentProcessingException;
import rw.igirepay.gateway.service.PaymentService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentProperties paymentProperties;

    public PaymentServiceImpl(PaymentProperties paymentProperties) {
        this.paymentProperties = paymentProperties;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request, String idempotencyKey) {
        log.info("Processing payment: key={}, amount={} {}",
                idempotencyKey, request.getAmount(), request.getCurrency());
        try {
            Thread.sleep(paymentProperties.getProcessingDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessingException("Payment processing was interrupted", e);
        }
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
}
