package rw.igirepay.gateway.service;

import rw.igirepay.gateway.model.AuditEvent;

import java.util.List;
import java.util.Map;

/**
 * Contract for recording and retrieving gateway audit events.
 * The implementation is swappable — in-memory for now, database-backed in production.
 */
public interface AuditService {

    /** Records a new audit event. Thread-safe; may be called from concurrent request threads. */
    void record(AuditEvent event);

    /** Returns all recorded events, most recent first. */
    List<AuditEvent> getAll();

    /** Returns all events for a specific idempotency key, most recent first. */
    List<AuditEvent> getByKey(String idempotencyKey);

    /** Returns a summary count grouped by event type — useful for dashboards. */
    Map<String, Long> getStats();
}
