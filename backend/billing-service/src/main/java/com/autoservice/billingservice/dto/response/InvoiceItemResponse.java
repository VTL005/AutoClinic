package com.autoservice.billingservice.dto.response;

import com.autoservice.billingservice.domain.enums.InvoiceItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceItemResponse(

        Long id,

        InvoiceItemType itemType,

        Long referenceId,

        String itemName,

        String description,

        BigDecimal quantity,

        BigDecimal unitPrice,

        BigDecimal lineTotal,

        LocalDateTime createdAt
) {
}