package com.portfolio.finrecon.api.dto;

import java.time.LocalDateTime;

import com.portfolio.finrecon.domain.ProcessingStatus;
import com.portfolio.finrecon.domain.UploadedFile;

public record UploadedFileResponse(
        Long id,
        String originalFilename,
        ProcessingStatus processingStatus,
        int totalCount,
        int validCount,
        int errorCount,
        int duplicateCount,
        LocalDateTime uploadedAt) {

    public static UploadedFileResponse from(UploadedFile file) {
        return new UploadedFileResponse(
                file.getId(),
                file.getOriginalFilename(),
                file.getProcessingStatus(),
                file.getTotalCount(),
                file.getValidCount(),
                file.getErrorCount(),
                file.getDuplicateCount(),
                file.getUploadedAt());
    }
}
