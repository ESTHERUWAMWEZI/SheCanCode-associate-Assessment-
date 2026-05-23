package rw.igirepay.gateway.model;

public enum AuditEventType {
    PAYMENT_PROCESSED,
    CACHE_HIT,
    PAYLOAD_CONFLICT,
    PAYMENT_FAILED,
    MISSING_IDEMPOTENCY_KEY
}
