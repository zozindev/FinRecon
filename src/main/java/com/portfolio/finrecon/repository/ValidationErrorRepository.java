package com.portfolio.finrecon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.finrecon.domain.ValidationError;

public interface ValidationErrorRepository extends JpaRepository<ValidationError, Long> {

    List<ValidationError> findByUploadedFileIdOrderByRowNumber(Long uploadedFileId);
}
