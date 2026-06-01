package com.portfolio.finrecon.api;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.finrecon.api.dto.ReconciliationResponse;
import com.portfolio.finrecon.api.dto.ReconciliationSummaryResponse;
import com.portfolio.finrecon.api.dto.RunDateRequest;
import com.portfolio.finrecon.common.response.ApiResponse;
import com.portfolio.finrecon.service.ReconciliationService;

@RestController
@RequestMapping("/api/v1/reconciliations")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping
    public ApiResponse<List<ReconciliationResponse>> run(@Valid @RequestBody RunDateRequest request) {
        return ApiResponse.of(reconciliationService.run(request.businessDate())
                .stream()
                .map(ReconciliationResponse::from)
                .toList());
    }

    @GetMapping
    public ApiResponse<List<ReconciliationResponse>> list(@RequestParam LocalDate businessDate) {
        return ApiResponse.of(reconciliationService.findByDate(businessDate)
                .stream()
                .map(ReconciliationResponse::from)
                .toList());
    }

    @GetMapping("/summary")
    public ApiResponse<ReconciliationSummaryResponse> summary(@RequestParam LocalDate businessDate) {
        return ApiResponse.of(reconciliationService.summarize(businessDate));
    }
}
