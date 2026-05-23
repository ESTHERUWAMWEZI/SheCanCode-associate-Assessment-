package rw.igirepay.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.igirepay.gateway.model.AuditEvent;
import rw.igirepay.gateway.service.AuditService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditEvent>> getAllEvents() {
        return ResponseEntity.ok(auditService.getAll());
    }

    @GetMapping("/{key}")
    public ResponseEntity<List<AuditEvent>> getEventsByKey(@PathVariable String key) {
        return ResponseEntity.ok(auditService.getByKey(key));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(auditService.getStats());
    }
}
