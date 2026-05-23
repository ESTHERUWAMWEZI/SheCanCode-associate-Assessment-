package rw.igirepay.gateway.service;

import rw.igirepay.gateway.model.AuditEvent;

import java.util.List;
import java.util.Map;

public interface AuditService {

    void record(AuditEvent event);

    List<AuditEvent> getAll();

    List<AuditEvent> getByKey(String idempotencyKey);

    Map<String, Long> getStats();
}
