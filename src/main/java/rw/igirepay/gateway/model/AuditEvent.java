package rw.igirepay.gateway.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Immutable record of a single gateway event.
 *
 * Each field maps to something an auditor, support engineer, or compliance
 * officer would need: who made the request (key), what they sent (amount/currency),
 * what happened (eventType), and when (timestamp).
 */
@Data
@Builder
public class AuditEvent {

    /** Auto-generated unique ID for this event. */
    private final String eventId;

    /** The idempotency key supplied by the client (may be null for MISSING_KEY events). */
    private final String idempotencyKey;

    /** Category of this event — see AuditEventType. */
    private final AuditEventType eventType;

    /** Payment amount from the request body (null if body was absent/malformed). */
    private final String amount;

    /** Payment currency from the request body. */
    private final String currency;

    /** Human-readable description of what happened. */
    private final String description;

    /** SHA-256 hash of the request body used for idempotency comparison. */
    private final String requestBodyHash;

    /** When this event was recorded. */
    private final LocalDateTime timestamp;
}
