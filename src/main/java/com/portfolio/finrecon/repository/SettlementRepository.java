package com.portfolio.finrecon.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.finrecon.domain.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByBusinessDate(LocalDate businessDate);

    void deleteByBusinessDate(LocalDate businessDate);
}
