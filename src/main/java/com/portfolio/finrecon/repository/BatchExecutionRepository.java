package com.portfolio.finrecon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.finrecon.domain.BatchExecution;

public interface BatchExecutionRepository extends JpaRepository<BatchExecution, Long> {

    List<BatchExecution> findAllByOrderByStartedAtDesc();
}
