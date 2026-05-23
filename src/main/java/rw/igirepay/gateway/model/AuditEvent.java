package rw.igirepay.gateway.model;

import java.time.LocalDateTime;

public class AuditEvent {

    private final String eventId;
    private final String idempotencyKey;
    private final AuditEventType eventType;
    private final String amount;
    private final String currency;
    private final String description;
    private final String requestBodyHash;
    private final LocalDateTime timestamp;

    private AuditEvent(Builder builder) {
        this.eventId = builder.eventId;
        this.idempotencyKey = builder.idempotencyKey;
        this.eventType = builder.eventType;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.description = builder.description;
        this.requestBodyHash = builder.requestBodyHash;
        this.timestamp = builder.timestamp;
    }

    public String getEventId() { return eventId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public AuditEventType getEventType() { return eventType; }
    public String getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public String getRequestBodyHash() { return requestBodyHash; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String eventId;
        private String idempotencyKey;
        private AuditEventType eventType;
        private String amount;
        private String currency;
        private String description;
        private String requestBodyHash;
        private LocalDateTime timestamp;

        public Builder eventId(String eventId) { this.eventId = eventId; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder eventType(AuditEventType eventType) { this.eventType = eventType; return this; }
        public Builder amount(String amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder requestBodyHash(String requestBodyHash) { this.requestBodyHash = requestBodyHash; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public AuditEvent build() { return new AuditEvent(this); }
    }
}
