package com.portfolio.finrecon.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.finrecon.api.dto.LoginRequest;
import com.portfolio.finrecon.api.dto.LoginResponse;
import com.portfolio.finrecon.common.exception.DomainException;
import com.portfolio.finrecon.domain.AppUser;
import com.portfolio.finrecon.repository.AppUserRepository;
import com.portfolio.finrecon.service.AuditService;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordHashService passwordHashService;
    private final JwtTokenService jwtTokenService;
    private final AuditService auditService;

    public AuthService(AppUserRepository appUserRepository, PasswordHashService passwordHashService,
            JwtTokenService jwtTokenService, AuditService auditService) {
        this.appUserRepository = appUserRepository;
        this.passwordHashService = passwordHashService;
        this.jwtTokenService = jwtTokenService;
        this.auditService = auditService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsernameAndActiveTrue(request.username())
                .orElseThrow(() -> invalidLogin());
        if (!passwordHashService.matches(request.password(), user.getPasswordHash())) {
            throw invalidLogin();
        }
        String token = jwtTokenService.create(user.getUsername(), user.getUserRole());
        auditService.record("LOGIN", "app_users", String.valueOf(user.getId()), "username=" + user.getUsername());
        return new LoginResponse("Bearer", token, jwtTokenService.getExpiresInSeconds(), user.getUsername(),
                user.getUserRole());
    }

    private DomainException invalidLogin() {
        return new DomainException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid username or password.");
    }
}
