package com.portfolio.finrecon.api.dto;

import java.time.LocalDateTime;

import com.portfolio.finrecon.domain.AuditLog;

public record AuditLogResponse(
        Long id,
        String actor,
        String actionType,
        String targetType,
        String targetId,
        String metadata,
        LocalDateTime occurredAt) {

    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActor(),
                auditLog.getActionType(),
                auditLog.getTargetType(),
                auditLog.getTargetId(),
                auditLog.getMetadata(),
                auditLog.getOccurredAt());
    }
}
