package com.portfolio.finrecon.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "batch_executions")
public class BatchExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobName;
    private LocalDate businessDate;

    @Enumerated(EnumType.STRING)
    private BatchExecutionStatus executionStatus;

    private int processedCount;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    protected BatchExecution() {
    }

    public BatchExecution(String jobName, LocalDate businessDate, LocalDateTime startedAt) {
        this.jobName = jobName;
        this.businessDate = businessDate;
        this.executionStatus = BatchExecutionStatus.SUCCESS;
        this.startedAt = startedAt;
    }

    public void complete(int processedCount) {
        this.executionStatus = BatchExecutionStatus.SUCCESS;
        this.processedCount = processedCount;
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getJobName() {
        return jobName;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public BatchExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public int getProcessedCount() {
        return processedCount;
    }
}
