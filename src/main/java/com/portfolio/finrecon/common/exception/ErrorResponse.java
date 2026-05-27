package com.portfolio.finrecon.common.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        List<FieldViolation> violations) {

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(Instant.now(), status, code, message, List.of());
    }

    public static ErrorResponse of(int status, String code, String message, List<FieldViolation> violations) {
        return new ErrorResponse(Instant.now(), status, code, message, violations);
    }

    public record FieldViolation(String field, String message) {
    }
}
