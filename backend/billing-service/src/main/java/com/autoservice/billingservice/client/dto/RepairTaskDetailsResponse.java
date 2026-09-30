package com.autoservice.billingservice.client.dto;

import java.math.BigDecimal;

public record RepairTaskDetailsResponse(

        Long id,

        String taskName,

        String description,

        BigDecimal laborHours,

        BigDecimal laborCost,

        String status
) {
}