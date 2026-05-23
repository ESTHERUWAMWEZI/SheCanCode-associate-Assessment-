package rw.igirepay.gateway.dto;

public class IdempotencyResult {

    private final PaymentResponse response;
    private final boolean cacheHit;
    private final int httpStatusCode;

    public IdempotencyResult(PaymentResponse response, boolean cacheHit, int httpStatusCode) {
        this.response = response;
        this.cacheHit = cacheHit;
        this.httpStatusCode = httpStatusCode;
    }

    public PaymentResponse getResponse() { return response; }
    public boolean isCacheHit() { return cacheHit; }
    public int getHttpStatusCode() { return httpStatusCode; }
}
