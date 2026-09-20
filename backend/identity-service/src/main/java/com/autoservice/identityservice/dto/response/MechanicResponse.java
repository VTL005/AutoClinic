package com.autoservice.identityservice.dto.response;

import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.EmploymentStatus;
import com.autoservice.identityservice.domain.enums.SkillLevel;

import java.math.BigDecimal;
import java.time.Instant;

public record MechanicResponse(
        Long profileId,
        Long userId,
        String username,
        String fullName,
        String phone,
        String email,
        AccountStatus accountStatus,
        SkillLevel skillLevel,
        String specialization,
        BigDecimal hourlyRate,
        EmploymentStatus employmentStatus,
        Instant createdAt,
        Instant updatedAt
) {
}