package com.portfolio.finrecon.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private String originalFilename;
    private String contentHash;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus;

    private int totalCount;
    private int validCount;
    private int errorCount;
    private int duplicateCount;
    private LocalDateTime uploadedAt;

    protected UploadedFile() {
    }

    public UploadedFile(FileType fileType, String originalFilename, String contentHash, LocalDateTime uploadedAt) {
        this.fileType = fileType;
        this.originalFilename = originalFilename;
        this.contentHash = contentHash;
        this.processingStatus = ProcessingStatus.COMPLETED;
        this.uploadedAt = uploadedAt;
    }

    public void finish(int totalCount, int validCount, int errorCount, int duplicateCount) {
        this.totalCount = totalCount;
        this.validCount = validCount;
        this.errorCount = errorCount;
        this.duplicateCount = duplicateCount;
        this.processingStatus = errorCount == 0 ? ProcessingStatus.COMPLETED : ProcessingStatus.COMPLETED_WITH_ERRORS;
    }

    public Long getId() {
        return id;
    }

    public FileType getFileType() {
        return fileType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getValidCount() {
        return validCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
