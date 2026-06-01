package com.portfolio.finrecon.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.portfolio.finrecon.common.exception.DomainException;
import com.portfolio.finrecon.domain.UserRole;

@Component
public class JwtTokenService {

    private final String secret;
    private final long expiresInSeconds;

    public JwtTokenService(
            @Value("${finrecon.auth.jwt-secret}") String secret,
            @Value("${finrecon.auth.expires-in-seconds:3600}") long expiresInSeconds) {
        this.secret = secret;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String create(String username, UserRole role) {
        long now = Instant.now().getEpochSecond();
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + username + "\",\"role\":\"" + role.name() + "\",\"iat\":" + now
                + ",\"exp\":" + (now + expiresInSeconds) + "}");
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    public AuthenticatedUser parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3 || !constantTimeEquals(sign(parts[0] + "." + parts[1]), parts[2])) {
            throw unauthorized("Invalid access token.");
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String username = extractString(payload, "sub");
        String role = extractString(payload, "role");
        long expiresAt = extractLong(payload, "exp");
        if (username == null || role == null || Instant.now().getEpochSecond() >= expiresAt) {
            throw unauthorized("Access token is expired or invalid.");
        }
        try {
            return new AuthenticatedUser(username, UserRole.valueOf(role));
        } catch (IllegalArgumentException exception) {
            throw unauthorized("Access token role is invalid.");
        }
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT signing failed.", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    private String extractString(String json, String field) {
        String token = "\"" + field + "\":\"";
        int start = json.indexOf(token);
        if (start < 0) {
            return null;
        }
        int valueStart = start + token.length();
        int valueEnd = json.indexOf("\"", valueStart);
        return valueEnd < 0 ? null : json.substring(valueStart, valueEnd);
    }

    private long extractLong(String json, String field) {
        String token = "\"" + field + "\":";
        int start = json.indexOf(token);
        if (start < 0) {
            return 0;
        }
        int valueStart = start + token.length();
        int valueEnd = json.indexOf(",", valueStart);
        if (valueEnd < 0) {
            valueEnd = json.indexOf("}", valueStart);
        }
        return Long.parseLong(json.substring(valueStart, valueEnd));
    }

    private DomainException unauthorized(String message) {
        return new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
    }
}
