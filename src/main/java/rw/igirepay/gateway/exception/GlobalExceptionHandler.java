package rw.igirepay.gateway.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rw.igirepay.gateway.dto.ErrorResponse;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Centralised exception-to-HTTP-response mapping for the entire application.
 *
 * Every custom exception is caught here and mapped to a consistent ErrorResponse.
 * No try/catch blocks are needed in controllers or services — exceptions propagate
 * naturally and are translated here into appropriate HTTP status codes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // 400 Bad Request
    // -------------------------------------------------------------------------

    @ExceptionHandler(MissingIdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleMissingKey(
            MissingIdempotencyKeyException ex, HttpServletRequest request) {

        log.warn("Missing Idempotency-Key header on {}", request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed JSON request body on {}", request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, "Request body is malformed or missing.", request);
    }

    // -------------------------------------------------------------------------
    // 405 Method Not Allowed
    // -------------------------------------------------------------------------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint.", request);
    }

    // -------------------------------------------------------------------------
    // 409 Conflict
    // -------------------------------------------------------------------------

    @ExceptionHandler(ConflictingIdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictingIdempotencyKeyException ex, HttpServletRequest request) {

        log.warn("Idempotency key conflict on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Entity — @Valid failures
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Validation failed on {}: {}", request.getRequestURI(), details);
        return build(HttpStatus.UNPROCESSABLE_ENTITY, details, request);
    }

    // -------------------------------------------------------------------------
    // 500 Internal Server Error
    // -------------------------------------------------------------------------

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentError(
            PaymentProcessingException ex, HttpServletRequest request) {

        log.error("Payment processing error on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support with the traceId.", request);
    }

    // -------------------------------------------------------------------------
    // Builder helper
    // -------------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .traceId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
