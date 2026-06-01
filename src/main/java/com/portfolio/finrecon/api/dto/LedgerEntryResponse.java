package com.portfolio.finrecon.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.portfolio.finrecon.domain.LedgerEntry;
import com.portfolio.finrecon.domain.PartnerStatus;

public record LedgerEntryResponse(
        Long id,
        String ledgerReferenceId,
        String transactionId,
        LocalDate recordDate,
        BigDecimal amount,
        PartnerStatus ledgerStatus) {

    public static LedgerEntryResponse from(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getLedgerReferenceId(),
                entry.getTransactionId(),
                entry.getRecordDate(),
                entry.getAmount(),
                entry.getLedgerStatus());
    }
}
