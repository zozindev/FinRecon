package com.portfolio.finrecon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.finrecon.domain.UploadedFile;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    boolean existsByContentHash(String contentHash);

    Optional<UploadedFile> findByContentHash(String contentHash);
}
