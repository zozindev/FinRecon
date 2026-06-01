package com.portfolio.finrecon.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "validation_errors")
public class ValidationError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_file_id")
    private UploadedFile uploadedFile;

    private int rowNumber;
    private String transactionId;
    private String fieldName;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime createdAt;

    protected ValidationError() {
    }

    public ValidationError(UploadedFile uploadedFile, int rowNumber, String transactionId, String fieldName,
            String errorCode, String errorMessage, LocalDateTime createdAt) {
        this.uploadedFile = uploadedFile;
        this.rowNumber = rowNumber;
        this.transactionId = transactionId;
        this.fieldName = fieldName;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
