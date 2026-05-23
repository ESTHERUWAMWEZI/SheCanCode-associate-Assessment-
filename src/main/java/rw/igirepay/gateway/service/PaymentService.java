package rw.igirepay.gateway.service;

import rw.igirepay.gateway.dto.PaymentRequest;
import rw.igirepay.gateway.dto.PaymentResponse;

/**
 * Contract for the downstream payment processor.
 * The implementation may simulate, call a real processor, or delegate to an external API.
 */
public interface PaymentService {

    /**
     * Executes a payment and returns the result.
     * This method is intentionally NOT idempotent — the idempotency guarantee
     * is enforced by the layer above (IdempotencyService), ensuring this
     * method is called exactly once per unique idempotency key.
     */
    PaymentResponse processPayment(PaymentRequest request, String idempotencyKey);
}
