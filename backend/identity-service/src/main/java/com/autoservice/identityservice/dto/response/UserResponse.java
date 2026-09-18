package com.autoservice.identityservice.dto.response;

import java.time.Instant;

import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String phone,
        String email,
        Role role,
        AccountStatus accountStatus,
        Instant createdAt
) {
}