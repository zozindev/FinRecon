package com.portfolio.finrecon.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.portfolio.finrecon.common.exception.DomainException;

@Component
public class PasswordHashService {

    public boolean matches(String rawPassword, String encodedHash) {
        String[] parts = encodedHash.split("\\$");
        if (parts.length == 4 && "pbkdf2-sha256".equals(parts[0])) {
            String calculated = pbkdf2(rawPassword, Integer.parseInt(parts[1]), parts[2]);
            return MessageDigest.isEqual(
                    calculated.getBytes(StandardCharsets.UTF_8),
                    parts[3].getBytes(StandardCharsets.UTF_8));
        }
        return MessageDigest.isEqual(
                sha256(rawPassword).getBytes(StandardCharsets.UTF_8),
                encodedHash.getBytes(StandardCharsets.UTF_8));
    }

    private String pbkdf2(String rawPassword, int iterations, String encodedSalt) {
        try {
            byte[] salt = Base64.getDecoder().decode(encodedSalt);
            KeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (Exception exception) {
            throw new DomainException(HttpStatus.INTERNAL_SERVER_ERROR, "PASSWORD_HASH_FAILED",
                    "Password hash verification failed.");
        }
    }

    private String sha256(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable.", exception);
        }
    }
}
