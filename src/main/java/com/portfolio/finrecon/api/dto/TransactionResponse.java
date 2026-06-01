package com.portfolio.finrecon.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.portfolio.finrecon.domain.ExternalTransaction;
import com.portfolio.finrecon.domain.PartnerStatus;
import com.portfolio.finrecon.domain.TransactionType;

public record TransactionResponse(
        Long id,
        String transactionId,
        TransactionType transactionType,
        String originalTransactionId,
        String merchantId,
        LocalDate transactionDate,
        BigDecimal amount,
        String maskedPaymentNumber,
        PartnerStatus partnerStatus) {

    public static TransactionResponse from(ExternalTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionId(),
                transaction.getTransactionType(),
                transaction.getOriginalTransactionId(),
                transaction.getMerchantId(),
                transaction.getTransactionDate(),
                transaction.getAmount(),
                transaction.getMaskedPaymentNumber(),
                transaction.getPartnerStatus());
    }
}
