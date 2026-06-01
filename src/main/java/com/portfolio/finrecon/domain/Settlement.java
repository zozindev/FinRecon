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
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate businessDate;
    private BigDecimal approvalAmount;
    private BigDecimal cancellationAmount;
    private BigDecimal grossAmount;
    private BigDecimal feeAmount;
    private BigDecimal payoutAmount;

    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus;

    private LocalDateTime confirmedAt;

    protected Settlement() {
    }

    public Settlement(LocalDate businessDate, BigDecimal approvalAmount, BigDecimal cancellationAmount,
            BigDecimal grossAmount, BigDecimal feeAmount, BigDecimal payoutAmount, LocalDateTime confirmedAt) {
        this.businessDate = businessDate;
        this.approvalAmount = approvalAmount;
        this.cancellationAmount = cancellationAmount;
        this.grossAmount = grossAmount;
        this.feeAmount = feeAmount;
        this.payoutAmount = payoutAmount;
        this.settlementStatus = SettlementStatus.CONFIRMED;
        this.confirmedAt = confirmedAt;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public BigDecimal getApprovalAmount() {
        return approvalAmount;
    }

    public BigDecimal getCancellationAmount() {
        return cancellationAmount;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public BigDecimal getPayoutAmount() {
        return payoutAmount;
    }
}
