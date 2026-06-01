package com.portfolio.finrecon.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.portfolio.finrecon.api.dto.UploadedFileResponse;
import com.portfolio.finrecon.common.response.ApiResponse;
import com.portfolio.finrecon.service.LedgerFileService;

@RestController
@RequestMapping("/api/v1/ledger-files")
public class LedgerFileController {

    private final LedgerFileService ledgerFileService;

    public LedgerFileController(LedgerFileService ledgerFileService) {
        this.ledgerFileService = ledgerFileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UploadedFileResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.of(UploadedFileResponse.from(ledgerFileService.upload(file)));
    }
}
