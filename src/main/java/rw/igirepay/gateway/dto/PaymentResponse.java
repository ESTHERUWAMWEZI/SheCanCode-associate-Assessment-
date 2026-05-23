package rw.igirepay.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private String message;
    private String transactionId;
    private String idempotencyKey;
    private String status;
    private String amount;
    private String currency;
    private LocalDateTime processedAt;

    private PaymentResponse() {}

    public String getMessage() { return message; }
    public String getTransactionId() { return transactionId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getStatus() { return status; }
    public String getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDateTime getProcessedAt() { return processedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String message;
        private String transactionId;
        private String idempotencyKey;
        private String status;
        private String amount;
        private String currency;
        private LocalDateTime processedAt;

        public Builder message(String message) { this.message = message; return this; }
        public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder amount(String amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder processedAt(LocalDateTime processedAt) { this.processedAt = processedAt; return this; }

        public PaymentResponse build() {
            PaymentResponse r = new PaymentResponse();
            r.message = this.message;
            r.transactionId = this.transactionId;
            r.idempotencyKey = this.idempotencyKey;
            r.status = this.status;
            r.amount = this.amount;
            r.currency = this.currency;
            r.processedAt = this.processedAt;
            return r;
        }
    }
}
