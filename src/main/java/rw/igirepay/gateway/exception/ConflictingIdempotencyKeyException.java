package rw.igirepay.gateway.exception;

public class ConflictingIdempotencyKeyException extends RuntimeException {

    public ConflictingIdempotencyKeyException(String message) {
        super(message);
    }
}
