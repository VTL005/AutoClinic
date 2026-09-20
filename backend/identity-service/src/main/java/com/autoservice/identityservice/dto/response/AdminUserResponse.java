package com.autoservice.identityservice.dto.response;

import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;

import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String username,
        String fullName,
        String phone,
        String email,
        Role role,
        AccountStatus accountStatus,
        Integer failedLoginCount,
        Instant lockedUntil,
        Instant createdAt,
        Instant updatedAt
) {
}