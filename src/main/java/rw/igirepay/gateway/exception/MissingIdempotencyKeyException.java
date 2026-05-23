package rw.igirepay.gateway.exception;

/**
 * Thrown when the Idempotency-Key request header is absent or blank.
 * Maps to HTTP 400 Bad Request via the global exception handler.
 */
public class MissingIdempotencyKeyException extends RuntimeException {

    public MissingIdempotencyKeyException(String message) {
        super(message);
    }
}
