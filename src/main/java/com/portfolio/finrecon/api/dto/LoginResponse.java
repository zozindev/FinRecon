package com.portfolio.finrecon.api.dto;

import com.portfolio.finrecon.domain.UserRole;

public record LoginResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        String username,
        UserRole role) {
}
