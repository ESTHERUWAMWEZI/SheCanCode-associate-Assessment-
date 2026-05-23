package rw.igirepay.gateway.model;

/**
 * Classifies every payment gateway event for audit trail purposes.
 *
 * Typed event categories make it trivial to filter logs, build dashboards,
 * and generate compliance reports without parsing free-text log messages.
 */
public enum AuditEventType {

    /** First-time request: payment was processed and cached. */
    PAYMENT_PROCESSED,

    /** Duplicate request: cached response returned without re-processing. */
    CACHE_HIT,

    /** Same key submitted with a different request body — rejected. */
    PAYLOAD_CONFLICT,

    /** Payment processing failed due to a downstream or system error. */
    PAYMENT_FAILED,

    /** Request arrived without the required Idempotency-Key header. */
    MISSING_IDEMPOTENCY_KEY
}
