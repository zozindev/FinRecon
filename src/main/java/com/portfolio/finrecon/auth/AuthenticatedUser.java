package com.portfolio.finrecon.auth;

import com.portfolio.finrecon.domain.UserRole;

public record AuthenticatedUser(String username, UserRole role) {

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
