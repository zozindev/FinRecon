package com.portfolio.finrecon.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.finrecon.domain.ExternalTransaction;

public interface ExternalTransactionRepository extends JpaRepository<ExternalTransaction, Long> {

    boolean existsByTransactionId(String transactionId);

    Optional<ExternalTransaction> findByTransactionId(String transactionId);

    List<ExternalTransaction> findByTransactionDate(LocalDate transactionDate);
}
