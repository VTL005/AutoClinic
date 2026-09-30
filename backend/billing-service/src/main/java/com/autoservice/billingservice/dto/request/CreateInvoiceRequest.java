package com.autoservice.billingservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateInvoiceRequest(

        @NotNull(
                message = "Mã phiếu sửa chữa không được để trống"
        )
        @Positive(
                message = "Mã phiếu sửa chữa phải lớn hơn 0"
        )
        Long repairOrderId,

        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Số tiền giảm giá không được nhỏ hơn 0"
        )
        @Digits(
                integer = 13,
                fraction = 2,
                message = "Số tiền giảm giá không đúng định dạng"
        )
        BigDecimal discountAmount,

        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Tiền thuế không được nhỏ hơn 0"
        )
        @Digits(
                integer = 13,
                fraction = 2,
                message = "Tiền thuế không đúng định dạng"
        )
        BigDecimal taxAmount,

        @Future(
                message = "Hạn thanh toán phải nằm trong tương lai"
        )
        LocalDateTime dueAt,

        @Size(
                max = 1000,
                message = "Ghi chú không được vượt quá 1000 ký tự"
        )
        String note
) {
}