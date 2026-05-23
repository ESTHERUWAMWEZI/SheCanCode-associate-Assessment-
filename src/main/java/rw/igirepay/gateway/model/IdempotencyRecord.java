package rw.igirepay.gateway.model;

import rw.igirepay.gateway.dto.PaymentResponse;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class IdempotencyRecord {

    private final String idempotencyKey;
    private final String requestBodyHash;
    private final CompletableFuture<PaymentResponse> processingFuture;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    private volatile PaymentResponse cachedResponse;
    private volatile IdempotencyStatus status;
    private volatile int httpStatusCode;

    private IdempotencyRecord(Builder builder) {
        this.idempotencyKey = builder.idempotencyKey;
        this.requestBodyHash = builder.requestBodyHash;
        this.processingFuture = builder.processingFuture;
        this.createdAt = builder.createdAt;
        this.expiresAt = builder.expiresAt;
        this.status = builder.status;
        this.httpStatusCode = builder.httpStatusCode;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRequestBodyHash() { return requestBodyHash; }
    public CompletableFuture<PaymentResponse> getProcessingFuture() { return processingFuture; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public PaymentResponse getCachedResponse() { return cachedResponse; }
    public IdempotencyStatus getStatus() { return status; }
    public int getHttpStatusCode() { return httpStatusCode; }

    public void setCachedResponse(PaymentResponse cachedResponse) { this.cachedResponse = cachedResponse; }
    public void setStatus(IdempotencyStatus status) { this.status = status; }
    public void setHttpStatusCode(int httpStatusCode) { this.httpStatusCode = httpStatusCode; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String idempotencyKey;
        private String requestBodyHash;
        private CompletableFuture<PaymentResponse> processingFuture;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private IdempotencyStatus status;
        private int httpStatusCode;

        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder requestBodyHash(String requestBodyHash) { this.requestBodyHash = requestBodyHash; return this; }
        public Builder processingFuture(CompletableFuture<PaymentResponse> processingFuture) { this.processingFuture = processingFuture; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder status(IdempotencyStatus status) { this.status = status; return this; }
        public Builder httpStatusCode(int httpStatusCode) { this.httpStatusCode = httpStatusCode; return this; }

        public IdempotencyRecord build() { return new IdempotencyRecord(this); }
    }
}
