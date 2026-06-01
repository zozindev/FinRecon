package com.portfolio.finrecon.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.finrecon.api.dto.BatchExecutionResponse;
import com.portfolio.finrecon.common.response.ApiResponse;
import com.portfolio.finrecon.repository.BatchExecutionRepository;

@RestController
@RequestMapping("/api/v1/batch-executions")
public class BatchExecutionController {

    private final BatchExecutionRepository batchExecutionRepository;

    public BatchExecutionController(BatchExecutionRepository batchExecutionRepository) {
        this.batchExecutionRepository = batchExecutionRepository;
    }

    @GetMapping
    public ApiResponse<List<BatchExecutionResponse>> list() {
        return ApiResponse.of(batchExecutionRepository.findAllByOrderByStartedAtDesc()
                .stream()
                .map(BatchExecutionResponse::from)
                .toList());
    }
}
