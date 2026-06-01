package com.portfolio.finrecon.common.status;

import com.portfolio.finrecon.common.response.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/status")
public class PlatformStatusController {

    @GetMapping
    public ApiResponse<PlatformStatusResponse> getStatus() {
        return ApiResponse.of(new PlatformStatusResponse("FinRecon", "CORE_WORKFLOW", "READY"));
    }

    public record PlatformStatusResponse(String application, String phase, String status) {
    }
}
