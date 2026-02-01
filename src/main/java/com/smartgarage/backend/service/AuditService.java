package com.smartgarage.backend.service;

import com.smartgarage.backend.model.AuditModule;

public interface AuditService {

    void log(
            AuditModule module,
            Long actorId,
            String actorEmail,
            String actorRole,
            String action,
            String entityType,
            Long entityId,
            String oldValue,
            String newValue
    );
}
