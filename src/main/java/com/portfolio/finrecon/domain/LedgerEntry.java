package com.portfolio.finrecon.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ledgerReferenceId;
    private String transactionId;
    private String merchantId;
    private LocalDate recordDate;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PartnerStatus ledgerStatus;

    private LocalDateTime createdAt;

    protected LedgerEntry() {
    }

    public LedgerEntry(String ledgerReferenceId, String transactionId, String merchantId, LocalDate recordDate,
            BigDecimal amount, PartnerStatus ledgerStatus, LocalDateTime createdAt) {
        this.ledgerReferenceId = ledgerReferenceId;
        this.transactionId = transactionId;
        this.merchantId = merchantId;
        this.recordDate = recordDate;
        this.amount = amount;
        this.ledgerStatus = ledgerStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getLedgerReferenceId() {
        return ledgerReferenceId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PartnerStatus getLedgerStatus() {
        return ledgerStatus;
    }
}
