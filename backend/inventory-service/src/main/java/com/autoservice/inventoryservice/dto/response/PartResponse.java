package com.autoservice.inventoryservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PartResponse(

        Long id,

        String partCode,

        String name,

        String description,

        String manufacturer,

        String unit,

        BigDecimal unitPrice,

        Integer quantityInStock,

        Integer minimumStock,

        boolean lowStock,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}