package rw.igirepay.gateway.service;

import rw.igirepay.gateway.dto.IdempotencyResult;
import rw.igirepay.gateway.dto.PaymentRequest;

/**
 * Contract for the idempotency gateway layer.
 * Guarantees exactly-once processing semantics for payment requests.
 */
public interface IdempotencyService {

    /**
     * Processes a payment request with idempotency guarantees.
     *
     * - First call with a key: processes the payment and caches the response.
     * - Retry with same key + same payload: returns cached response instantly.
     * - Same key + different payload: throws ConflictingIdempotencyKeyException.
     * - Concurrent calls with same key: second caller waits for first to finish.
     */
    IdempotencyResult processWithIdempotency(String idempotencyKey, PaymentRequest request);
}
