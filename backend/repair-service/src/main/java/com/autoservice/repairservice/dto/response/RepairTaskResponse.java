package com.autoservice.repairservice.dto.response;

import com.autoservice.repairservice.domain.enums.RepairTaskStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RepairTaskResponse(

        Long id,

        String taskName,

        String description,

        RepairTaskStatus status,

        BigDecimal laborHours,

        BigDecimal laborCost,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}