package rw.igirepay.gateway.exception;

/**
 * Thrown when the downstream payment processor encounters an unrecoverable error.
 * Maps to HTTP 500 Internal Server Error via the global exception handler.
 */
public class PaymentProcessingException extends RuntimeException {

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
