package rw.igirepay.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.igirepay.gateway.model.AuditEvent;
import rw.igirepay.gateway.service.AuditService;

import java.util.List;
import java.util.Map;

/**
 * Read-only audit trail endpoint.
 *
 * In production, this would be secured behind an admin role (e.g. @PreAuthorize).
 * Exposing the audit log publicly here is intentional for demonstration purposes.
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    /**
     * GET /api/v1/audit
     * Returns all recorded audit events, most recent first.
     */
    @GetMapping
    public ResponseEntity<List<AuditEvent>> getAllEvents() {
        return ResponseEntity.ok(auditService.getAll());
    }

    /**
     * GET /api/v1/audit/{key}
     * Returns all events for a specific idempotency key.
     * Useful for debugging: "show me everything that happened with this key."
     */
    @GetMapping("/{key}")
    public ResponseEntity<List<AuditEvent>> getEventsByKey(@PathVariable String key) {
        return ResponseEntity.ok(auditService.getByKey(key));
    }

    /**
     * GET /api/v1/audit/stats
     * Returns a count of events grouped by type.
     * Powers a simple dashboard: total payments, cache hit rate, error rate.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(auditService.getStats());
    }
}
