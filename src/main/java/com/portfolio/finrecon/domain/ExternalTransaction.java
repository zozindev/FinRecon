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
@Table(name = "external_transactions")
public class ExternalTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_file_id")
    private UploadedFile uploadedFile;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private String originalTransactionId;
    private String merchantId;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String maskedPaymentNumber;

    @Enumerated(EnumType.STRING)
    private PartnerStatus partnerStatus;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus;

    private LocalDateTime createdAt;

    protected ExternalTransaction() {
    }

    public ExternalTransaction(String transactionId, UploadedFile uploadedFile, TransactionType transactionType,
            String originalTransactionId, String merchantId, LocalDate transactionDate, BigDecimal amount,
            String maskedPaymentNumber, PartnerStatus partnerStatus, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.uploadedFile = uploadedFile;
        this.transactionType = transactionType;
        this.originalTransactionId = originalTransactionId;
        this.merchantId = merchantId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.maskedPaymentNumber = maskedPaymentNumber;
        this.partnerStatus = partnerStatus;
        this.processingStatus = ProcessingStatus.COMPLETED;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMaskedPaymentNumber() {
        return maskedPaymentNumber;
    }

    public PartnerStatus getPartnerStatus() {
        return partnerStatus;
    }
}
