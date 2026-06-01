package com.portfolio.finrecon.api.dto;

import java.time.LocalDate;

import com.portfolio.finrecon.domain.ReconciliationResult;
import com.portfolio.finrecon.domain.ReconciliationResultType;

public record ReconciliationResponse(
        Long id,
        LocalDate businessDate,
        String transactionId,
        ReconciliationResultType resultType) {

    public static ReconciliationResponse from(ReconciliationResult result) {
        return new ReconciliationResponse(
                result.getId(),
                result.getBusinessDate(),
                result.getTransactionId(),
                result.getResultType());
    }
}
