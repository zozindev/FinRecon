package com.portfolio.finrecon.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.finrecon.api.dto.BatchExecutionResponse;
import com.portfolio.finrecon.batch.FinReconBatchJobService;
import com.portfolio.finrecon.common.exception.DomainException;
import com.portfolio.finrecon.common.response.ApiResponse;
import com.portfolio.finrecon.domain.BatchExecution;
import com.portfolio.finrecon.repository.BatchExecutionRepository;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/batch-executions")
public class BatchExecutionController {

    private final BatchExecutionRepository batchExecutionRepository;
    private final FinReconBatchJobService batchJobService;

    public BatchExecutionController(BatchExecutionRepository batchExecutionRepository,
            FinReconBatchJobService batchJobService) {
        this.batchExecutionRepository = batchExecutionRepository;
        this.batchJobService = batchJobService;
    }

    @GetMapping
    public ApiResponse<List<BatchExecutionResponse>> list() {
        return ApiResponse.of(batchExecutionRepository.findAllByOrderByStartedAtDesc()
                .stream()
                .map(BatchExecutionResponse::from)
                .toList());
    }

    @PostMapping("/{executionId}/retry")
    public ApiResponse<BatchExecutionResponse> retry(@PathVariable Long executionId) {
        BatchExecution execution = batchExecutionRepository.findById(executionId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "BATCH_EXECUTION_NOT_FOUND",
                        "Batch execution not found."));

        if ("DAILY_RECONCILIATION".equals(execution.getJobName())) {
            batchJobService.runDailyReconciliation(execution.getBusinessDate());
        } else if ("DAILY_SETTLEMENT".equals(execution.getJobName())) {
            batchJobService.runDailySettlement(execution.getBusinessDate());
        } else {
            throw new DomainException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_BATCH_JOB",
                    "Unsupported batch job cannot be retried.");
        }

        BatchExecution latestExecution = batchExecutionRepository.findAllByOrderByStartedAtDesc()
                .stream()
                .filter(candidate -> candidate.getJobName().equals(execution.getJobName()))
                .filter(candidate -> candidate.getBusinessDate().equals(execution.getBusinessDate()))
                .findFirst()
                .orElseThrow();
        return ApiResponse.of(BatchExecutionResponse.from(latestExecution));
    }
}
