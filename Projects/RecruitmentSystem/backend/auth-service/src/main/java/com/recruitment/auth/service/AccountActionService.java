package com.recruitment.auth.service;

import com.recruitment.auth.config.AccountActionProperties;
import com.recruitment.auth.entity.*;
import com.recruitment.auth.exception.BusinessException;
import com.recruitment.auth.exception.ErrorCode;
import com.recruitment.auth.repository.AccountActionTokenRepository;
import com.recruitment.auth.repository.RefreshTokenRepository;
import com.recruitment.auth.repository.UserRepository;
import com.recruitment.auth.security.RefreshTokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountActionService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AccountActionTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenHasher tokenHasher;
    private final AccountActionTokenDelivery delivery;
    private final AccountActionProperties properties;

    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email.trim().toLowerCase()).ifPresent(user ->
                issueIfCooldownElapsed(user, AccountActionPurpose.PASSWORD_RESET));
    }

    public void resetPassword(String rawToken, String newPassword) {
        AccountActionToken token = consume(rawToken, AccountActionPurpose.PASSWORD_RESET);
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        refreshTokenRepository.deleteAllByUser(user);
    }

    public void resendVerification(String email) {
        userRepository.findByEmail(email.trim().toLowerCase())
                .filter(user -> !Boolean.TRUE.equals(user.getVerified()))
                .ifPresent(user -> issueIfCooldownElapsed(user, AccountActionPurpose.EMAIL_VERIFICATION));
    }

    public void verifyEmail(String rawToken) {
        AccountActionToken token = consume(rawToken, AccountActionPurpose.EMAIL_VERIFICATION);
        User user = token.getUser();
        user.setVerified(true);
        userRepository.save(user);
    }

    public void issueRegistrationVerification(User user) {
        if (!Boolean.TRUE.equals(user.getVerified())) {
            issue(user, AccountActionPurpose.EMAIL_VERIFICATION);
        }
    }

    private void issueIfCooldownElapsed(User user, AccountActionPurpose purpose) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(properties.getIssuanceCooldownSeconds());
        boolean coolingDown = tokenRepository.findFirstByUserAndPurposeOrderByCreatedAtDesc(user, purpose)
                .map(token -> token.getCreatedAt() != null && token.getCreatedAt().isAfter(threshold))
                .orElse(false);
        if (!coolingDown) {
            issue(user, purpose);
        }
    }

    private void issue(User user, AccountActionPurpose purpose) {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.findAllByUserAndPurposeAndUsedAtIsNullAndRevokedAtIsNull(user, purpose)
                .forEach(token -> token.setRevokedAt(now));

        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        long expiryMinutes = purpose == AccountActionPurpose.PASSWORD_RESET
                ? properties.getPasswordResetExpiryMinutes()
                : properties.getEmailVerificationExpiryMinutes();
        tokenRepository.save(AccountActionToken.builder()
                .user(user)
                .purpose(purpose)
                .tokenHash(tokenHasher.hash(rawToken))
                .expiresAt(now.plusMinutes(expiryMinutes))
                .build());
        delivery.deliver(user, purpose, rawToken);
    }

    private AccountActionToken consume(String rawToken, AccountActionPurpose purpose) {
        AccountActionToken token = tokenRepository.findByTokenHashAndPurpose(tokenHasher.hash(rawToken), purpose)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTION_TOKEN_INVALID));
        LocalDateTime now = LocalDateTime.now();
        if (token.getUsedAt() != null || token.getRevokedAt() != null) {
            throw new BusinessException(ErrorCode.ACTION_TOKEN_INVALID);
        }
        if (token.getExpiresAt().isBefore(now)) {
            token.setRevokedAt(now);
            throw new BusinessException(ErrorCode.ACTION_TOKEN_EXPIRED);
        }
        token.setUsedAt(now);
        return token;
    }
}
