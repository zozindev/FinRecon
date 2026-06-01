package com.portfolio.finrecon.api.dto;

import java.time.LocalDate;

import com.portfolio.finrecon.domain.BatchExecution;
import com.portfolio.finrecon.domain.BatchExecutionStatus;

public record BatchExecutionResponse(
        Long id,
        String jobName,
        LocalDate businessDate,
        BatchExecutionStatus executionStatus,
        int processedCount) {

    public static BatchExecutionResponse from(BatchExecution execution) {
        return new BatchExecutionResponse(
                execution.getId(),
                execution.getJobName(),
                execution.getBusinessDate(),
                execution.getExecutionStatus(),
                execution.getProcessedCount());
    }
}
