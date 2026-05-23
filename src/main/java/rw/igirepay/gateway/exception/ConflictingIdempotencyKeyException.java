package rw.igirepay.gateway.exception;

/**
 * Thrown when a client reuses an idempotency key with a different request body.
 * Maps to HTTP 409 Conflict via the global exception handler.
 */
public class ConflictingIdempotencyKeyException extends RuntimeException {

    public ConflictingIdempotencyKeyException(String message) {
        super(message);
    }
}
