package com.portfolio.finrecon.auth;

import java.util.Optional;

public final class SecurityContext {

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void set(AuthenticatedUser user) {
        CURRENT_USER.set(user);
    }

    public static Optional<AuthenticatedUser> currentUser() {
        return Optional.ofNullable(CURRENT_USER.get());
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
