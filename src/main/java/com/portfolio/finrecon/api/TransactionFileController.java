package com.portfolio.finrecon.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.portfolio.finrecon.api.dto.UploadedFileResponse;
import com.portfolio.finrecon.common.exception.DomainException;
import com.portfolio.finrecon.common.response.ApiResponse;
import com.portfolio.finrecon.repository.UploadedFileRepository;
import com.portfolio.finrecon.service.TransactionFileService;

@RestController
@RequestMapping("/api/v1/transaction-files")
public class TransactionFileController {

    private final TransactionFileService transactionFileService;
    private final UploadedFileRepository uploadedFileRepository;

    public TransactionFileController(TransactionFileService transactionFileService,
            UploadedFileRepository uploadedFileRepository) {
        this.transactionFileService = transactionFileService;
        this.uploadedFileRepository = uploadedFileRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UploadedFileResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.of(UploadedFileResponse.from(transactionFileService.upload(file)));
    }

    @GetMapping("/{fileId}")
    public ApiResponse<UploadedFileResponse> get(@PathVariable Long fileId) {
        return ApiResponse.of(uploadedFileRepository.findById(fileId)
                .map(UploadedFileResponse::from)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "Uploaded file not found.")));
    }
}
