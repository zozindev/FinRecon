package com.portfolio.finrecon.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.finrecon.api.dto.AuditLogResponse;
import com.portfolio.finrecon.common.response.ApiResponse;
import com.portfolio.finrecon.repository.AuditLogRepository;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ApiResponse<List<AuditLogResponse>> list() {
        return ApiResponse.of(auditLogRepository.findAllByOrderByOccurredAtDesc()
                .stream()
                .map(AuditLogResponse::from)
                .toList());
    }
}
