package com.portfolio.finrecon.api.dto;

import com.portfolio.finrecon.domain.ValidationError;

public record ValidationErrorResponse(
        Long id,
        int rowNumber,
        String transactionId,
        String fieldName,
        String errorCode,
        String errorMessage) {

    public static ValidationErrorResponse from(ValidationError error) {
        return new ValidationErrorResponse(
                error.getId(),
                error.getRowNumber(),
                error.getTransactionId(),
                error.getFieldName(),
                error.getErrorCode(),
                error.getErrorMessage());
    }
}
