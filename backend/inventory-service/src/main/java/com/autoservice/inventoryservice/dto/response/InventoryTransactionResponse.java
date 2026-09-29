package com.autoservice.inventoryservice.dto.response;

import com.autoservice.inventoryservice.domain.enums.InventoryTransactionType;

import java.time.LocalDateTime;

public record InventoryTransactionResponse(

        Long id,

        Long partId,

        String partCode,

        InventoryTransactionType transactionType,

        Integer quantity,

        Integer quantityBefore,

        Integer quantityAfter,

        String referenceType,

        Long referenceId,

        String note,

        Long performedByUserId,

        LocalDateTime createdAt
) {
}