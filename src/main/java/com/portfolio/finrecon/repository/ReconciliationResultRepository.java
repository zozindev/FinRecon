package com.portfolio.finrecon.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.finrecon.domain.ReconciliationResult;
import com.portfolio.finrecon.domain.ReconciliationResultType;

public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, Long> {

    void deleteByBusinessDate(LocalDate businessDate);

    List<ReconciliationResult> findByBusinessDateOrderByTransactionId(LocalDate businessDate);

    List<ReconciliationResult> findByBusinessDateAndResultType(LocalDate businessDate, ReconciliationResultType resultType);
}
