package com.smartgarage.backend.repository;

import com.smartgarage.backend.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop20ByOrderByTimestampDesc();

    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
            String entityType,
            Long entityId
    );
}
