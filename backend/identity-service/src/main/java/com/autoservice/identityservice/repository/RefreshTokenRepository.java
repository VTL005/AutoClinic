package com.autoservice.identityservice.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoservice.identityservice.domain.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(
            Long userId
    );

    long deleteByExpiresAtBefore(
            Instant currentTime
    );
}