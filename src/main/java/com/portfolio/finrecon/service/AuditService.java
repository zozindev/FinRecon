package com.portfolio.finrecon.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.portfolio.finrecon.auth.SecurityContext;
import com.portfolio.finrecon.domain.AuditLog;
import com.portfolio.finrecon.repository.AuditLogRepository;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String actionType, String targetType, String targetId, String metadata) {
        String actor = SecurityContext.currentUser()
                .map(user -> user.username())
                .orElse("system");
        auditLogRepository.save(new AuditLog(actor, actionType, targetType, targetId, metadata, LocalDateTime.now()));
    }
}
