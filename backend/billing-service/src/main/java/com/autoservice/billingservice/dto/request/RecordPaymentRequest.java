package com.autoservice.billingservice.dto.request;

import com.autoservice.billingservice.domain.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RecordPaymentRequest(

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
                message = "Phương thức thanh toán không được để trống"
        )
        PaymentMethod paymentMethod,

        @Size(
                max = 150,
                message = "Mã giao dịch không được vượt quá 150 ký tự"
        )
        String transactionReference,

        @Size(
                max = 1000,
                message = "Ghi chú không được vượt quá 1000 ký tự"
        )
        String note
) {
}