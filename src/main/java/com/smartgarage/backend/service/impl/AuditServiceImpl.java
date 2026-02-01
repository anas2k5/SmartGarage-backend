package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.model.AuditLog;
import com.smartgarage.backend.model.AuditModule;
import com.smartgarage.backend.repository.AuditLogRepository;
import com.smartgarage.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(
            AuditModule module,
            Long actorId,
            String actorEmail,
            String actorRole,
            String action,
            String entityType,
            Long entityId,
            String oldValue,
            String newValue
    ) {
        AuditLog log = AuditLog.builder()
                .module(module)
                .actorId(actorId)
                .actorEmail(actorEmail)
                .actorRole(actorRole)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }
}
