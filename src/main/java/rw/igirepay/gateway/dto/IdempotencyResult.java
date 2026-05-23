package rw.igirepay.gateway.dto;

import lombok.Value;

/**
 * Internal result returned by the idempotency service to the controller.
 *
 * Keeping cacheHit and httpStatusCode here — rather than embedding them in
 * PaymentResponse — preserves the separation between the API contract (what
 * the client sees) and the gateway's internal routing decisions (what the
 * controller uses to set headers and status codes).
 */
@Value
public class IdempotencyResult {

    PaymentResponse response;

    /** True when the response was served from the idempotency cache. */
    boolean cacheHit;

    /** The original HTTP status code to replay on cache hits. */
    int httpStatusCode;
}
