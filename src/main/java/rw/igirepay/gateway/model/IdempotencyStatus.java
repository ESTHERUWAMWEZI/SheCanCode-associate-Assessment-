package rw.igirepay.gateway.model;

/**
 * Lifecycle states of an idempotency record.
 *
 * PROCESSING — the first request is actively being handled; any concurrent
 *              duplicate must wait rather than trigger a second payment.
 *
 * COMPLETED  — the payment finished; the cached response can be returned
 *              immediately to any retry without re-processing.
 */
public enum IdempotencyStatus {
    PROCESSING,
    COMPLETED
}
