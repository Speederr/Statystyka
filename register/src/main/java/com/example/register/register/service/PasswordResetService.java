package com.example.register.register.service;

import com.example.register.register.model.PasswordResetToken;
import com.example.register.register.model.User;
import com.example.register.register.repository.PasswordResetTokenRepository;
import com.example.register.register.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_TTL_MINUTES = 30;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public Optional<String> createResetTokenForEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();
        passwordResetTokenRepository.deleteByUser(user);

        String rawToken = generateSecureToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES));

        passwordResetTokenRepository.save(token);
        return Optional.of(rawToken);
    }

    @Transactional
    public boolean resetPasswordWithToken(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }

        String tokenHash = hashToken(rawToken);
        Optional<PasswordResetToken> optionalToken = passwordResetTokenRepository.findByTokenHash(tokenHash);
        if (optionalToken.isEmpty()) {
            return false;
        }

        PasswordResetToken token = optionalToken.get();
        LocalDateTime now = LocalDateTime.now();
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(now)) {
            return false;
        }

        userService.updateUserPassword(token.getUser().getUsername(), newPassword);
        token.setUsedAt(now);
        passwordResetTokenRepository.save(token);
        return true;
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
