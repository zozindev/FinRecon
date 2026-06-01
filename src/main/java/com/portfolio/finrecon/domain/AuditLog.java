package com.portfolio.finrecon.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actor;
    private String actionType;
    private String targetType;
    private String targetId;
    private String metadata;
    private LocalDateTime occurredAt;

    protected AuditLog() {
    }

    public AuditLog(String actor, String actionType, String targetType, String targetId, String metadata,
            LocalDateTime occurredAt) {
        this.actor = actor;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.metadata = metadata;
        this.occurredAt = occurredAt;
    }
}
