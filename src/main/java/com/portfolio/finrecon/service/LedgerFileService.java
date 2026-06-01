package com.portfolio.finrecon.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.portfolio.finrecon.common.exception.DomainException;
import com.portfolio.finrecon.domain.FileType;
import com.portfolio.finrecon.domain.LedgerEntry;
import com.portfolio.finrecon.domain.PartnerStatus;
import com.portfolio.finrecon.domain.UploadedFile;
import com.portfolio.finrecon.domain.ValidationError;
import com.portfolio.finrecon.repository.LedgerEntryRepository;
import com.portfolio.finrecon.repository.UploadedFileRepository;
import com.portfolio.finrecon.repository.ValidationErrorRepository;
import com.portfolio.finrecon.service.CsvFileReader.CsvRow;

@Service
public class LedgerFileService {

    private static final List<String> HEADER = List.of(
            "ledgerReferenceId",
            "transactionId",
            "merchantId",
            "recordDate",
            "amount",
            "status");

    private final UploadedFileRepository uploadedFileRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final ValidationErrorRepository validationErrorRepository;
    private final CsvFileReader csvFileReader;
    private final FileHashService fileHashService;
    private final AuditService auditService;

    public LedgerFileService(UploadedFileRepository uploadedFileRepository,
            LedgerEntryRepository ledgerEntryRepository,
            ValidationErrorRepository validationErrorRepository,
            CsvFileReader csvFileReader,
            FileHashService fileHashService,
            AuditService auditService) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.validationErrorRepository = validationErrorRepository;
        this.csvFileReader = csvFileReader;
        this.fileHashService = fileHashService;
        this.auditService = auditService;
    }

    @Transactional
    public UploadedFile upload(MultipartFile file) {
        byte[] content = bytes(file);
        String hash = fileHashService.sha256(content);
        if (uploadedFileRepository.existsByContentHash(hash)) {
            throw new DomainException(HttpStatus.CONFLICT, "DUPLICATE_FILE", "The same file has already been uploaded.");
        }

        UploadedFile uploadedFile = uploadedFileRepository.save(new UploadedFile(
                FileType.LEDGER,
                file.getOriginalFilename() == null ? "ledger-entries.csv" : file.getOriginalFilename(),
                hash,
                LocalDateTime.now()));

        List<CsvRow> rows = csvFileReader.read(content, HEADER);
        Set<String> seenReferences = new HashSet<>();
        Set<String> seenTransactions = new HashSet<>();
        int duplicateCount = 0;
        int validCount = 0;
        int errorCount = 0;

        for (CsvRow row : rows) {
            ValidationError error = validate(uploadedFile, row, seenReferences, seenTransactions);
            if (error != null) {
                validationErrorRepository.save(error);
                errorCount++;
                if (error.getErrorCode().startsWith("DUPLICATE")) {
                    duplicateCount++;
                }
                continue;
            }
            ledgerEntryRepository.save(toLedgerEntry(row));
            seenReferences.add(row.get("ledgerReferenceId"));
            seenTransactions.add(row.get("transactionId"));
            validCount++;
        }

        uploadedFile.finish(rows.size(), validCount, errorCount, duplicateCount);
        auditService.record("UPLOAD_LEDGER_FILE", "uploaded_files", String.valueOf(uploadedFile.getId()),
                "valid=" + validCount + ", errors=" + errorCount);
        return uploadedFile;
    }

    private ValidationError validate(UploadedFile uploadedFile, CsvRow row, Set<String> seenReferences,
            Set<String> seenTransactions) {
        if (row.get("ledgerReferenceId").isBlank()) {
            return error(uploadedFile, row, "ledgerReferenceId", "REQUIRED_FIELD", "ledgerReferenceId is required.");
        }
        if (seenReferences.contains(row.get("ledgerReferenceId"))
                || ledgerEntryRepository.existsByLedgerReferenceId(row.get("ledgerReferenceId"))) {
            return error(uploadedFile, row, "ledgerReferenceId", "DUPLICATE_LEDGER_REFERENCE",
                    "ledgerReferenceId must be unique.");
        }
        if (row.get("transactionId").isBlank()) {
            return error(uploadedFile, row, "transactionId", "REQUIRED_FIELD", "transactionId is required.");
        }
        if (seenTransactions.contains(row.get("transactionId"))
                || ledgerEntryRepository.existsByTransactionId(row.get("transactionId"))) {
            return error(uploadedFile, row, "transactionId", "DUPLICATE_TRANSACTION", "transactionId must be unique.");
        }
        if (row.get("merchantId").isBlank()) {
            return error(uploadedFile, row, "merchantId", "REQUIRED_FIELD", "merchantId is required.");
        }
        if (parseDate(row.get("recordDate")) == null) {
            return error(uploadedFile, row, "recordDate", "INVALID_DATE", "recordDate must be yyyy-MM-dd.");
        }
        BigDecimal amount = parseAmount(row.get("amount"));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return error(uploadedFile, row, "amount", "INVALID_AMOUNT", "amount must be greater than zero.");
        }
        if (parseStatus(row.get("status")) == null) {
            return error(uploadedFile, row, "status", "INVALID_STATUS", "status must be APPROVED or CANCELED.");
        }
        return null;
    }

    private LedgerEntry toLedgerEntry(CsvRow row) {
        return new LedgerEntry(
                row.get("ledgerReferenceId"),
                row.get("transactionId"),
                row.get("merchantId"),
                LocalDate.parse(row.get("recordDate")),
                parseAmount(row.get("amount")),
                PartnerStatus.valueOf(row.get("status")),
                LocalDateTime.now());
    }

    private ValidationError error(UploadedFile uploadedFile, CsvRow row, String field, String code, String message) {
        return new ValidationError(uploadedFile, row.rowNumber(), row.get("transactionId"), field, code, message,
                LocalDateTime.now());
    }

    private byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "INVALID_FILE", "Uploaded file cannot be read.");
        }
    }

    private BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value).setScale(2);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private PartnerStatus parseStatus(String value) {
        try {
            return PartnerStatus.valueOf(value);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
