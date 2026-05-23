package rw.igirepay.gateway.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rw.igirepay.gateway.model.IdempotencyRecord;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyStore {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyStore.class);

    private final ConcurrentHashMap<String, IdempotencyRecord> store = new ConcurrentHashMap<>();

    public IdempotencyRecord get(String key) {
        return store.get(key);
    }

    public IdempotencyRecord putIfAbsent(String key, IdempotencyRecord record) {
        return store.putIfAbsent(key, record);
    }

    public void remove(String key) {
        store.remove(key);
    }

    public int size() {
        return store.size();
    }

    @Scheduled(fixedDelayString = "${igirepay.idempotency.eviction-interval-ms:60000}")
    public void evictExpired() {
        LocalDateTime now = LocalDateTime.now();
        int before = store.size();
        store.entrySet().removeIf(entry -> entry.getValue().getExpiresAt().isBefore(now));
        int evicted = before - store.size();
        if (evicted > 0) {
            log.info("TTL eviction: removed {} expired records, {} remaining", evicted, store.size());
        }
    }
}
