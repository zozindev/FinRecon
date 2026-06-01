package com.portfolio.finrecon.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.finrecon.api.dto.ValidationErrorResponse;
import com.portfolio.finrecon.common.response.ApiResponse;
import com.portfolio.finrecon.repository.ValidationErrorRepository;

@RestController
@RequestMapping("/api/v1/validation-errors")
public class ValidationErrorController {

    private final ValidationErrorRepository validationErrorRepository;

    public ValidationErrorController(ValidationErrorRepository validationErrorRepository) {
        this.validationErrorRepository = validationErrorRepository;
    }

    @GetMapping
    public ApiResponse<List<ValidationErrorResponse>> list(@RequestParam(required = false) Long fileId) {
        List<ValidationErrorResponse> errors = (fileId == null
                ? validationErrorRepository.findAll()
                : validationErrorRepository.findByUploadedFileIdOrderByRowNumber(fileId))
                .stream()
                .map(ValidationErrorResponse::from)
                .toList();
        return ApiResponse.of(errors);
    }
}
