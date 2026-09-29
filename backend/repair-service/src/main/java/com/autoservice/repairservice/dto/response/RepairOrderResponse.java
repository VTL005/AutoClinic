package com.autoservice.repairservice.dto.response;

import com.autoservice.repairservice.domain.enums.RepairOrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record RepairOrderResponse(

        Long id,

        String repairCode,

        Long bookingId,

        Long customerUserId,

        Long vehicleId,

        Long mechanicUserId,

        Long createdByUserId,

        String serviceType,

        String customerComplaint,

        String diagnosis,

        String technicianNote,

        Integer odometer,

        RepairOrderStatus status,

        LocalDateTime receivedAt,

        LocalDateTime estimatedCompletionAt,

        LocalDateTime startedAt,

        LocalDateTime completedAt,

        LocalDateTime deliveredAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        List<RepairTaskResponse> tasks,

        List<RepairStatusHistoryResponse> statusHistory
) {
}