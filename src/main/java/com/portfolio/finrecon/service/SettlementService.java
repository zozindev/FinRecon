package com.portfolio.finrecon.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.finrecon.domain.ExternalTransaction;
import com.portfolio.finrecon.domain.ReconciliationResult;
import com.portfolio.finrecon.domain.ReconciliationResultType;
import com.portfolio.finrecon.domain.Settlement;
import com.portfolio.finrecon.domain.TransactionType;
import com.portfolio.finrecon.repository.BatchExecutionRepository;
import com.portfolio.finrecon.repository.ReconciliationResultRepository;
import com.portfolio.finrecon.repository.SettlementRepository;
import com.portfolio.finrecon.domain.BatchExecution;

@Service
public class SettlementService {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.025");

    private final ReconciliationResultRepository reconciliationResultRepository;
    private final SettlementRepository settlementRepository;
    private final BatchExecutionRepository batchExecutionRepository;
    private final AuditService auditService;

    public SettlementService(ReconciliationResultRepository reconciliationResultRepository,
            SettlementRepository settlementRepository,
            BatchExecutionRepository batchExecutionRepository,
            AuditService auditService) {
        this.reconciliationResultRepository = reconciliationResultRepository;
        this.settlementRepository = settlementRepository;
        this.batchExecutionRepository = batchExecutionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Settlement runDaily(LocalDate businessDate) {
        BatchExecution execution = batchExecutionRepository.save(
                new BatchExecution("DAILY_SETTLEMENT", businessDate, LocalDateTime.now()));
        settlementRepository.deleteByBusinessDate(businessDate);

        List<ReconciliationResult> matchedResults = reconciliationResultRepository.findByBusinessDateAndResultType(
                businessDate,
                ReconciliationResultType.MATCHED);

        BigDecimal approvalAmount = BigDecimal.ZERO.setScale(2);
        BigDecimal cancellationAmount = BigDecimal.ZERO.setScale(2);
        for (ReconciliationResult result : matchedResults) {
            ExternalTransaction transaction = result.getExternalTransaction();
            if (transaction == null) {
                continue;
            }
            if (transaction.getTransactionType() == TransactionType.APPROVAL) {
                approvalAmount = approvalAmount.add(transaction.getAmount());
            } else if (transaction.getTransactionType() == TransactionType.CANCEL) {
                cancellationAmount = cancellationAmount.add(transaction.getAmount());
            }
        }

        BigDecimal grossAmount = approvalAmount.subtract(cancellationAmount).setScale(2);
        BigDecimal feeAmount = grossAmount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal payoutAmount = grossAmount.subtract(feeAmount).setScale(2);
        Settlement settlement = settlementRepository.save(new Settlement(
                businessDate,
                approvalAmount,
                cancellationAmount,
                grossAmount,
                feeAmount,
                payoutAmount,
                LocalDateTime.now()));

        execution.complete(matchedResults.size());
        auditService.record("RUN_SETTLEMENT", "business_date", businessDate.toString(),
                "matched=" + matchedResults.size() + ", payout=" + payoutAmount);
        return settlement;
    }
}
