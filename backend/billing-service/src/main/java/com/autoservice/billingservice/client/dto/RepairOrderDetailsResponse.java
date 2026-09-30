package com.autoservice.billingservice.client.dto;

import java.util.List;

public record RepairOrderDetailsResponse(

        Long id,

        String repairCode,

        Long bookingId,

        Long customerUserId,

        Long vehicleId,

        Long mechanicUserId,

        String serviceType,

        String status,

        List<RepairTaskDetailsResponse> tasks
) {
}