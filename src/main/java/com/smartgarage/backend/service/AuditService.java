package com.smartgarage.backend.service;

public interface AuditService {

    void log(
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
