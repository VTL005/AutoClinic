package com.autoservice.billingservice.dto.request;

import com.autoservice.billingservice.domain.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InitiateOnlinePaymentRequest(

        @NotNull(
                message = "Số tiền thanh toán không được để trống"
        )
        @DecimalMin(
                value = "0.01",
                inclusive = true,
                message = "Số tiền thanh toán phải lớn hơn 0"
        )
        @Digits(
                integer = 13,
                fraction = 2,
                message = "Số tiền thanh toán không đúng định dạng"
        )
        BigDecimal amount,

        @NotNull(
                message = "Cổng thanh toán không được để trống"
        )
        PaymentMethod paymentMethod
) {
}