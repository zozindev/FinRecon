package com.portfolio.finrecon.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.finrecon.api.dto.TransactionResponse;
import com.portfolio.finrecon.common.exception.DomainException;
import com.portfolio.finrecon.common.response.ApiResponse;
import com.portfolio.finrecon.repository.ExternalTransactionRepository;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final ExternalTransactionRepository transactionRepository;

    public TransactionController(ExternalTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public ApiResponse<List<TransactionResponse>> list() {
        return ApiResponse.of(transactionRepository.findAll()
                .stream()
                .map(TransactionResponse::from)
                .toList());
    }

    @GetMapping("/{transactionId}")
    public ApiResponse<TransactionResponse> get(@PathVariable String transactionId) {
        return ApiResponse.of(transactionRepository.findByTransactionId(transactionId)
                .map(TransactionResponse::from)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "TRANSACTION_NOT_FOUND",
                        "Transaction not found.")));
    }
}
