package com.portfolio.finrecon.api;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.finrecon.api.dto.RunDateRequest;
import com.portfolio.finrecon.api.dto.SettlementResponse;
import com.portfolio.finrecon.common.response.ApiResponse;
import com.portfolio.finrecon.repository.SettlementRepository;
import com.portfolio.finrecon.service.SettlementService;

@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementController {

    private final SettlementService settlementService;
    private final SettlementRepository settlementRepository;

    public SettlementController(SettlementService settlementService, SettlementRepository settlementRepository) {
        this.settlementService = settlementService;
        this.settlementRepository = settlementRepository;
    }

    @PostMapping("/daily")
    public ApiResponse<SettlementResponse> runDaily(@Valid @RequestBody RunDateRequest request) {
        return ApiResponse.of(SettlementResponse.from(settlementService.runDaily(request.businessDate())));
    }

    @GetMapping
    public ApiResponse<List<SettlementResponse>> list() {
        return ApiResponse.of(settlementRepository.findAll()
                .stream()
                .map(SettlementResponse::from)
                .toList());
    }
}
