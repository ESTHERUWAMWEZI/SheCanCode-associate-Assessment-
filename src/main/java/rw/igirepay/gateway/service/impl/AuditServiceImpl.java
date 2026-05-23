package rw.igirepay.gateway.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rw.igirepay.gateway.model.AuditEvent;
import rw.igirepay.gateway.service.AuditService;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);
    private static final int MAX_EVENTS = 10_000;

    private final Deque<AuditEvent> eventLog = new ConcurrentLinkedDeque<>();

    @Override
    public void record(AuditEvent event) {
        if (eventLog.size() >= MAX_EVENTS) {
            eventLog.pollLast();
        }
        eventLog.addFirst(event);
        log.info("[AUDIT] type={} key={} amount={} {} desc=\"{}\"",
                event.getEventType(), event.getIdempotencyKey(),
                event.getAmount(), event.getCurrency(), event.getDescription());
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
