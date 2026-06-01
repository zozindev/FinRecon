package com.portfolio.finrecon.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.finrecon.api.dto.ReconciliationSummaryResponse;
import com.portfolio.finrecon.domain.BatchExecution;
import com.portfolio.finrecon.domain.ExternalTransaction;
import com.portfolio.finrecon.domain.LedgerEntry;
import com.portfolio.finrecon.domain.ReconciliationResult;
import com.portfolio.finrecon.domain.ReconciliationResultType;
import com.portfolio.finrecon.repository.BatchExecutionRepository;
import com.portfolio.finrecon.repository.ExternalTransactionRepository;
import com.portfolio.finrecon.repository.LedgerEntryRepository;
import com.portfolio.finrecon.repository.ReconciliationResultRepository;

@Service
public class ReconciliationService {

    private final ExternalTransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final ReconciliationResultRepository reconciliationResultRepository;
    private final BatchExecutionRepository batchExecutionRepository;
    private final AuditService auditService;

    public ReconciliationService(ExternalTransactionRepository transactionRepository,
            LedgerEntryRepository ledgerEntryRepository,
            ReconciliationResultRepository reconciliationResultRepository,
            BatchExecutionRepository batchExecutionRepository,
            AuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.reconciliationResultRepository = reconciliationResultRepository;
        this.batchExecutionRepository = batchExecutionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public List<ReconciliationResult> run(LocalDate businessDate) {
        BatchExecution execution = batchExecutionRepository.save(
                new BatchExecution("DAILY_RECONCILIATION", businessDate, LocalDateTime.now()));
        reconciliationResultRepository.deleteByBusinessDate(businessDate);

        Map<String, ExternalTransaction> transactions = transactionRepository.findByTransactionDate(businessDate)
                .stream()
                .collect(Collectors.toMap(ExternalTransaction::getTransactionId, Function.identity()));
        Map<String, LedgerEntry> ledgerEntries = ledgerEntryRepository.findByRecordDate(businessDate)
                .stream()
                .collect(Collectors.toMap(LedgerEntry::getTransactionId, Function.identity()));

        Set<String> transactionIds = new HashSet<>();
        transactionIds.addAll(transactions.keySet());
        transactionIds.addAll(ledgerEntries.keySet());

        List<ReconciliationResult> results = transactionIds.stream()
                .sorted()
                .map(transactionId -> compare(businessDate, transactionId, transactions.get(transactionId),
                        ledgerEntries.get(transactionId)))
                .toList();
        List<ReconciliationResult> saved = reconciliationResultRepository.saveAll(results);

        execution.complete(saved.size());
        auditService.record("RUN_RECONCILIATION", "business_date", businessDate.toString(),
                "processed=" + saved.size());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ReconciliationResult> findByDate(LocalDate businessDate) {
        return reconciliationResultRepository.findByBusinessDateOrderByTransactionId(businessDate);
    }

    @Transactional(readOnly = true)
    public ReconciliationSummaryResponse summarize(LocalDate businessDate) {
        List<ReconciliationResult> results = findByDate(businessDate);
        Map<ReconciliationResultType, Long> counts = new EnumMap<>(ReconciliationResultType.class);
        for (ReconciliationResultType type : ReconciliationResultType.values()) {
            counts.put(type, 0L);
        }
        for (ReconciliationResult result : results) {
            counts.compute(result.getResultType(), (key, count) -> count == null ? 1L : count + 1);
        }
        return new ReconciliationSummaryResponse(businessDate, results.size(), counts);
    }

    private ReconciliationResult compare(LocalDate businessDate, String transactionId,
            ExternalTransaction transaction, LedgerEntry ledgerEntry) {
        ReconciliationResultType resultType;
        BigDecimal comparedAmount = null;

        if (transaction == null) {
            resultType = ReconciliationResultType.TRANSACTION_MISSING;
            comparedAmount = ledgerEntry.getAmount();
        } else if (ledgerEntry == null) {
            resultType = ReconciliationResultType.LEDGER_MISSING;
            comparedAmount = transaction.getAmount();
        } else if (transaction.getAmount().compareTo(ledgerEntry.getAmount()) != 0) {
            resultType = ReconciliationResultType.AMOUNT_MISMATCH;
            comparedAmount = transaction.getAmount().subtract(ledgerEntry.getAmount());
        } else if (!transaction.getPartnerStatus().name().equals(ledgerEntry.getLedgerStatus().name())) {
            resultType = ReconciliationResultType.STATUS_MISMATCH;
        } else {
            resultType = ReconciliationResultType.MATCHED;
            comparedAmount = transaction.getAmount();
        }

        return new ReconciliationResult(businessDate, transactionId, transaction, ledgerEntry, resultType,
                comparedAmount, LocalDateTime.now());
    }
}
