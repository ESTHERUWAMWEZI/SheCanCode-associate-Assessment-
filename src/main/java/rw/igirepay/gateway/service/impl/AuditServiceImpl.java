package rw.igirepay.gateway.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rw.igirepay.gateway.model.AuditEvent;
import rw.igirepay.gateway.service.AuditService;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * In-memory audit log backed by a ConcurrentLinkedDeque.
 *
 * ConcurrentLinkedDeque provides thread-safe, non-blocking insertions from
 * multiple concurrent request threads without synchronisation overhead.
 * Events are stored newest-first by pushing to the front (addFirst).
 *
 * Production upgrade path: replace eventLog with a JPA repository backed
 * by PostgreSQL and use an async @EventListener to avoid adding latency
 * to the payment request path.
 */
@Slf4j
@Service
public class AuditServiceImpl implements AuditService {

    private static final int MAX_EVENTS = 10_000;

    private final Deque<AuditEvent> eventLog = new ConcurrentLinkedDeque<>();

    @Override
    public void record(AuditEvent event) {
        // Evict oldest event if cap is reached, preventing unbounded memory growth
        if (eventLog.size() >= MAX_EVENTS) {
            eventLog.pollLast();
        }

        eventLog.addFirst(event);

        log.info("[AUDIT] type={} key={} amount={} {} desc=\"{}\"",
                event.getEventType(),
                event.getIdempotencyKey(),
                event.getAmount(),
                event.getCurrency(),
                event.getDescription());
    }

    @Override
    public List<AuditEvent> getAll() {
        return List.copyOf(eventLog);
    }

    @Override
    public List<AuditEvent> getByKey(String idempotencyKey) {
        return eventLog.stream()
                .filter(e -> idempotencyKey.equals(e.getIdempotencyKey()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getStats() {
        return eventLog.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEventType().name(),
                        Collectors.counting()
                ));
    }
}
