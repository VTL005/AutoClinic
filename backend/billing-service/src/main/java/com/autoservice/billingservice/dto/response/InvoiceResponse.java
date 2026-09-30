package com.autoservice.billingservice.dto.response;

import com.autoservice.billingservice.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(

        Long id,

        String invoiceCode,

        Long repairOrderId,

        Long customerUserId,

        Long vehicleId,

        Long createdByUserId,

        BigDecimal subtotal,

        BigDecimal discountAmount,

        BigDecimal taxAmount,

        BigDecimal totalAmount,

        BigDecimal paidAmount,

        BigDecimal balanceDue,

        InvoiceStatus status,

        LocalDateTime issuedAt,

        LocalDateTime dueAt,

        LocalDateTime paidAt,

        String note,

        List<InvoiceItemResponse> items,

        List<PaymentResponse> payments,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}