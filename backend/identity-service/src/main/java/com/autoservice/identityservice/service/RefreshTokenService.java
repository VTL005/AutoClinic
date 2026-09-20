package com.autoservice.identityservice.service;

import com.autoservice.identityservice.config.JwtProperties;
import com.autoservice.identityservice.domain.entity.RefreshToken;
import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.exception.InvalidRefreshTokenException;
import com.autoservice.identityservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_LENGTH_BYTES = 48;
    private static final String HASH_ALGORITHM = "SHA-256";

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public IssuedRefreshToken issue(User user) {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        Instant expiresAt = Instant.now().plus(
                jwtProperties.refreshTokenTtl()
        );

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(
                rawToken,
                expiresAt
        );
    }

    @Transactional(readOnly = true)
    public RefreshToken requireUsable(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(
                                "Refresh token không hợp lệ."
                        )
                );

        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException(
                    "Refresh token đã bị thu hồi."
            );
        }

        if (refreshToken.isExpired()) {
            throw new InvalidRefreshTokenException(
                    "Refresh token đã hết hạn."
            );
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(RefreshToken refreshToken) {
        if (!refreshToken.isRevoked()) {
            refreshToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(refreshToken);
        }
    }

    @Transactional
    public void revoke(String rawToken) {
        String tokenHash = hashToken(rawToken);

        refreshTokenRepository
                .findByTokenHash(tokenHash)
                .ifPresent(this::revoke);
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        List<RefreshToken> activeTokens =
                refreshTokenRepository
                        .findAllByUserIdAndRevokedAtIsNull(userId);

        Instant revokedAt = Instant.now();

        activeTokens.forEach(token ->
                token.setRevokedAt(revokedAt)
        );

        refreshTokenRepository.saveAll(activeTokens);
    }

    @Transactional
    public long deleteExpiredTokens() {
        return refreshTokenRepository
                .deleteByExpiresAtBefore(Instant.now());
    }

    private String generateRawToken() {
        byte[] randomBytes =
                new byte[TOKEN_LENGTH_BYTES];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException(
                    "Refresh token không được để trống."
            );
        }

        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance(
                            HASH_ALGORITHM
                    );

            byte[] digest = messageDigest.digest(
                    rawToken.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Không thể khởi tạo thuật toán SHA-256.",
                    exception
            );
        }
    }

    public record IssuedRefreshToken(
            String token,
            Instant expiresAt
    ) {
    }
}