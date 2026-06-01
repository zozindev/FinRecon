package com.portfolio.finrecon.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reconciliation_results")
public class ReconciliationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate businessDate;
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_transaction_id")
    private ExternalTransaction externalTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_entry_id")
    private LedgerEntry ledgerEntry;

    @Enumerated(EnumType.STRING)
    private ReconciliationResultType resultType;

    private BigDecimal comparedAmount;
    private LocalDateTime executedAt;

    protected ReconciliationResult() {
    }

    public ReconciliationResult(LocalDate businessDate, String transactionId, ExternalTransaction externalTransaction,
            LedgerEntry ledgerEntry, ReconciliationResultType resultType, BigDecimal comparedAmount,
            LocalDateTime executedAt) {
        this.businessDate = businessDate;
        this.transactionId = transactionId;
        this.externalTransaction = externalTransaction;
        this.ledgerEntry = ledgerEntry;
        this.resultType = resultType;
        this.comparedAmount = comparedAmount;
        this.executedAt = executedAt;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public ExternalTransaction getExternalTransaction() {
        return externalTransaction;
    }

    public ReconciliationResultType getResultType() {
        return resultType;
    }
}
