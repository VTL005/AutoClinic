package com.autoservice.billingservice.dto.response;

import com.autoservice.billingservice.domain.enums.PaymentMethod;
import com.autoservice.billingservice.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(

        Long id,

        String paymentCode,

        Long invoiceId,

        BigDecimal amount,

        PaymentMethod paymentMethod,

        String providerOrderCode,

        String checkoutUrl,

        String qrCode,

        LocalDateTime expiresAt,

        PaymentStatus status,

        String transactionReference,

        LocalDateTime paidAt,

        LocalDateTime webhookReceivedAt,

        String failureReason,

        Long receivedByUserId,

        String note,

        LocalDateTime createdAt
) {
}