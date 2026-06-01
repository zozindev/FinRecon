package com.portfolio.finrecon.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.portfolio.finrecon.domain.AuditLog;
import com.portfolio.finrecon.repository.AuditLogRepository;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String actionType, String targetType, String targetId, String metadata) {
        auditLogRepository.save(new AuditLog("system", actionType, targetType, targetId, metadata, LocalDateTime.now()));
    }
}
