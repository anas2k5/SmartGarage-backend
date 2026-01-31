package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.AuditLog;
import com.smartgarage.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminAuditController {

    private final AuditLogRepository auditLogRepository;

    // 🔍 Recent platform activity (Last 20 actions)
    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentLogs() {
        return ResponseEntity.ok(
                auditLogRepository.findTop20ByOrderByTimestampDesc()
        );
    }

    // 🔎 Track history of a specific entity
    // Example: /api/admin/audit/BOOKING/12
    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable Long entityId
    ) {
        return ResponseEntity.ok(
                auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                        entityType,
                        entityId
                )
        );
    }
}
