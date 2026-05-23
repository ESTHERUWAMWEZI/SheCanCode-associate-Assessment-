package rw.igirepay.gateway.model;

import lombok.Builder;
import lombok.Data;
import rw.igirepay.gateway.dto.PaymentResponse;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Core domain entity stored in the idempotency cache for each unique key.
 *
 * Thread-safety note: this record is stored in a ConcurrentHashMap, but the
 * CompletableFuture field provides the synchronisation point for concurrent
 * requests — callers wait on the future rather than busy-polling the record.
 */
@Data
@Builder
public class IdempotencyRecord {

    /** The unique idempotency key provided by the client in the request header. */
    private final String idempotencyKey;

    /**
     * SHA-256 hash of the raw request body JSON.
     * Used to detect same-key / different-payload conflicts (→ 409 Conflict).
     */
    private final String requestBodyHash;

    /**
     * Cached response for COMPLETED records.
     * Null while status is PROCESSING.
     */
    private volatile PaymentResponse cachedResponse;

    /**
     * Lifecycle state: PROCESSING while the first request runs,
     * COMPLETED once the response is cached.
     */
    private volatile IdempotencyStatus status;

    /**
     * The original HTTP status code to replay on cache hits.
     * Ensures retries receive identical responses — body AND status.
     */
    private volatile int httpStatusCode;

    /**
     * Concurrent requests await this future instead of triggering duplicate processing.
     * Completed by the first thread when payment finishes; all waiting threads unblock.
     */
    private final CompletableFuture<PaymentResponse> processingFuture;

    /** When this record was first created. Used for audit and TTL calculation. */
    private final LocalDateTime createdAt;

    /**
     * Absolute expiry timestamp.
     * The TTL eviction scheduler removes records where LocalDateTime.now() is after expiresAt.
     */
    private final LocalDateTime expiresAt;
}
