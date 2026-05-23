package rw.igirepay.gateway.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rw.igirepay.gateway.model.IdempotencyRecord;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory store backed by ConcurrentHashMap.
 *
 * ConcurrentHashMap was chosen over a synchronized HashMap because it uses
 * segment-level locking (Java 8+: lock-free reads via volatile + CAS writes),
 * allowing many threads to read concurrently while only locking the specific
 * bucket being written. This is critical for a high-throughput payment gateway.
 *
 * The TTL eviction task prevents unbounded memory growth — an essential
 * production concern that is often overlooked in interview projects.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyStore {

    private final ConcurrentHashMap<String, IdempotencyRecord> store = new ConcurrentHashMap<>();

    /**
     * Retrieves a record by idempotency key, or null if absent.
     */
    public IdempotencyRecord get(String key) {
        return store.get(key);
    }

    /**
     * Atomically inserts the record only if the key is not already present.
     *
     * Returns null  → key was absent; this caller is the first (process the payment).
     * Returns existing → key already exists; caller must handle duplicate logic.
     *
     * This atomicity is the cornerstone of the concurrency guarantee — no lock
     * is needed around this call because ConcurrentHashMap.putIfAbsent() is atomic.
     */
    public IdempotencyRecord putIfAbsent(String key, IdempotencyRecord record) {
        return store.putIfAbsent(key, record);
    }

    /**
     * Removes a record — used when a payment fails so the client can retry.
     */
    public void remove(String key) {
        store.remove(key);
    }

    /**
     * Returns the current number of stored records.
     * Exposed for health checks and metrics.
     */
    public int size() {
        return store.size();
    }

    /**
     * Background TTL eviction task.
     *
     * Runs every eviction-interval-ms (default 60 seconds). Scans the map and
     * removes all entries where expiresAt is in the past. ConcurrentHashMap
     * allows safe removal during iteration via the entrySet iterator.
     *
     * Without this, a busy gateway would exhaust heap memory as idempotency
     * records accumulate indefinitely.
     */
    @Scheduled(fixedDelayString = "${igirepay.idempotency.eviction-interval-ms:60000}")
    public void evictExpired() {
        LocalDateTime now = LocalDateTime.now();
        int before = store.size();

        store.entrySet().removeIf(entry -> entry.getValue().getExpiresAt().isBefore(now));

        int evicted = before - store.size();
        if (evicted > 0) {
            log.info("TTL eviction complete: removed {} expired records, {} remaining", evicted, store.size());
        }
    }
}
