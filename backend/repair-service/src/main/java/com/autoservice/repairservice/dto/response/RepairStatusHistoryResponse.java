package com.autoservice.repairservice.dto.response;

import com.autoservice.repairservice.domain.enums.RepairOrderStatus;

import java.time.LocalDateTime;

public record RepairStatusHistoryResponse(

        Long id,

        RepairOrderStatus previousStatus,

        RepairOrderStatus newStatus,

        Long changedByUserId,

        String note,

        LocalDateTime changedAt
) {
}