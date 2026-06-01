package com.portfolio.finrecon.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.portfolio.finrecon.common.exception.DomainException;
import com.portfolio.finrecon.domain.ExternalTransaction;
import com.portfolio.finrecon.domain.FileType;
import com.portfolio.finrecon.domain.PartnerStatus;
import com.portfolio.finrecon.domain.TransactionType;
import com.portfolio.finrecon.domain.UploadedFile;
import com.portfolio.finrecon.domain.ValidationError;
import com.portfolio.finrecon.repository.ExternalTransactionRepository;
import com.portfolio.finrecon.repository.UploadedFileRepository;
import com.portfolio.finrecon.repository.ValidationErrorRepository;
import com.portfolio.finrecon.service.CsvFileReader.CsvRow;

@Service
public class TransactionFileService {

    private static final List<String> HEADER = List.of(
            "transactionId",
            "transactionType",
            "originalTransactionId",
            "merchantId",
            "transactionDate",
            "amount",
            "maskedPaymentNumber",
            "status");

    private final UploadedFileRepository uploadedFileRepository;
    private final ExternalTransactionRepository transactionRepository;
    private final ValidationErrorRepository validationErrorRepository;
    private final CsvFileReader csvFileReader;
    private final FileHashService fileHashService;
    private final AuditService auditService;

    public TransactionFileService(UploadedFileRepository uploadedFileRepository,
            ExternalTransactionRepository transactionRepository,
            ValidationErrorRepository validationErrorRepository,
            CsvFileReader csvFileReader,
            FileHashService fileHashService,
            AuditService auditService) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.transactionRepository = transactionRepository;
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
                FileType.TRANSACTION,
                originalFilename(file),
                hash,
                LocalDateTime.now()));

        List<CsvRow> rows = csvFileReader.read(content, HEADER);
        Set<String> seenIds = new HashSet<>();
        Map<String, BigDecimal> approvalAmounts = new HashMap<>();
        int duplicateCount = 0;
        int validCount = 0;
        int errorCount = 0;

        for (CsvRow row : rows) {
            ValidationError error = validate(row, uploadedFile, seenIds, approvalAmounts);
            if (error != null) {
                validationErrorRepository.save(error);
                errorCount++;
                if ("DUPLICATE_TRANSACTION".equals(error.getErrorCode())) {
                    duplicateCount++;
                }
                continue;
            }
            ExternalTransaction transaction = toTransaction(row, uploadedFile);
            transactionRepository.save(transaction);
            seenIds.add(transaction.getTransactionId());
            if (transaction.getTransactionType() == TransactionType.APPROVAL) {
                approvalAmounts.put(transaction.getTransactionId(), transaction.getAmount());
            }
            validCount++;
        }

        uploadedFile.finish(rows.size(), validCount, errorCount, duplicateCount);
        auditService.record("UPLOAD_TRANSACTION_FILE", "uploaded_files", String.valueOf(uploadedFile.getId()),
                "valid=" + validCount + ", errors=" + errorCount);
        return uploadedFile;
    }

    private ValidationError validate(CsvRow row, UploadedFile uploadedFile, Set<String> seenIds,
            Map<String, BigDecimal> approvalAmounts) {
        String transactionId = row.get("transactionId");
        if (transactionId.isBlank()) {
            return error(uploadedFile, row, "transactionId", "REQUIRED_FIELD", "transactionId is required.");
        }
        if (seenIds.contains(transactionId) || transactionRepository.existsByTransactionId(transactionId)) {
            return error(uploadedFile, row, "transactionId", "DUPLICATE_TRANSACTION", "transactionId must be unique.");
        }
        TransactionType type = parseEnum(TransactionType.class, row.get("transactionType"));
        if (type == null) {
            return error(uploadedFile, row, "transactionType", "INVALID_TRANSACTION_TYPE",
                    "transactionType must be APPROVAL or CANCEL.");
        }
        if (row.get("merchantId").isBlank()) {
            return error(uploadedFile, row, "merchantId", "REQUIRED_FIELD", "merchantId is required.");
        }
        BigDecimal amount = parseAmount(row.get("amount"));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return error(uploadedFile, row, "amount", "INVALID_AMOUNT", "amount must be greater than zero.");
        }
        if (parseDate(row.get("transactionDate")) == null) {
            return error(uploadedFile, row, "transactionDate", "INVALID_DATE", "transactionDate must be yyyy-MM-dd.");
        }
        if (!row.get("maskedPaymentNumber").contains("*")) {
            return error(uploadedFile, row, "maskedPaymentNumber", "UNMASKED_PAYMENT_NUMBER",
                    "payment number must be masked.");
        }
        if (parseEnum(PartnerStatus.class, row.get("status")) == null) {
            return error(uploadedFile, row, "status", "INVALID_STATUS", "status must be APPROVED or CANCELED.");
        }
        if (type == TransactionType.CANCEL) {
            String originalId = row.get("originalTransactionId");
            if (originalId.isBlank()) {
                return error(uploadedFile, row, "originalTransactionId", "REQUIRED_FIELD",
                        "originalTransactionId is required for cancellation.");
            }
            BigDecimal originalAmount = approvalAmounts.get(originalId);
            if (originalAmount == null) {
                originalAmount = transactionRepository.findByTransactionId(originalId)
                        .filter(transaction -> transaction.getTransactionType() == TransactionType.APPROVAL)
                        .map(ExternalTransaction::getAmount)
                        .orElse(null);
            }
            if (originalAmount == null || amount.compareTo(originalAmount) > 0) {
                return error(uploadedFile, row, "amount", "INVALID_CANCEL_AMOUNT",
                        "cancellation amount cannot exceed the original approval amount.");
            }
        }
        return null;
    }

    private ExternalTransaction toTransaction(CsvRow row, UploadedFile uploadedFile) {
        return new ExternalTransaction(
                row.get("transactionId"),
                uploadedFile,
                TransactionType.valueOf(row.get("transactionType")),
                blankToNull(row.get("originalTransactionId")),
                row.get("merchantId"),
                LocalDate.parse(row.get("transactionDate")),
                parseAmount(row.get("amount")),
                row.get("maskedPaymentNumber"),
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

    private String originalFilename(MultipartFile file) {
        return file.getOriginalFilename() == null ? "transactions.csv" : file.getOriginalFilename();
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

    private <T extends Enum<T>> T parseEnum(Class<T> type, String value) {
        try {
            return Enum.valueOf(type, value);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String blankToNull(String value) {
        return value.isBlank() ? null : value;
    }
}
