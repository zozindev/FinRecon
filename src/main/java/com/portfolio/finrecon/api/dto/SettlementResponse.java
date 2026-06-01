package com.portfolio.finrecon.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.portfolio.finrecon.domain.Settlement;

public record SettlementResponse(
        Long id,
        LocalDate businessDate,
        BigDecimal approvalAmount,
        BigDecimal cancellationAmount,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal payoutAmount) {

    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getBusinessDate(),
                settlement.getApprovalAmount(),
                settlement.getCancellationAmount(),
                settlement.getGrossAmount(),
                settlement.getFeeAmount(),
                settlement.getPayoutAmount());
    }
}
