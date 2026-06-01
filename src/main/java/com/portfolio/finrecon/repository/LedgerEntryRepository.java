package com.portfolio.finrecon.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.finrecon.domain.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    boolean existsByLedgerReferenceId(String ledgerReferenceId);

    boolean existsByTransactionId(String transactionId);

    Optional<LedgerEntry> findByTransactionId(String transactionId);

    List<LedgerEntry> findByRecordDate(LocalDate recordDate);
}
