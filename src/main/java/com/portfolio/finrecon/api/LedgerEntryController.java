package com.portfolio.finrecon.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.finrecon.api.dto.LedgerEntryResponse;
import com.portfolio.finrecon.common.response.ApiResponse;
import com.portfolio.finrecon.repository.LedgerEntryRepository;

@RestController
@RequestMapping("/api/v1/ledger-entries")
public class LedgerEntryController {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerEntryController(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @GetMapping
    public ApiResponse<List<LedgerEntryResponse>> list() {
        return ApiResponse.of(ledgerEntryRepository.findAll()
                .stream()
                .map(LedgerEntryResponse::from)
                .toList());
    }
}
