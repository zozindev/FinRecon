package com.portfolio.finrecon.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.portfolio.finrecon.common.exception.DomainException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;

    public JwtAuthInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isPublic(request)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Bearer access token is required.");
        }

        AuthenticatedUser user = jwtTokenService.parse(authorization.substring("Bearer ".length()));
        if (request.getRequestURI().startsWith("/api/v1/audit-logs") && !user.isAdmin()) {
            throw new DomainException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Admin role is required.");
        }
        SecurityContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        SecurityContext.clear();
    }

    private boolean isPublic(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.equals("/api/v1/status")
                || uri.equals("/api/v1/auth/login")
                || uri.startsWith("/actuator");
    }
}
