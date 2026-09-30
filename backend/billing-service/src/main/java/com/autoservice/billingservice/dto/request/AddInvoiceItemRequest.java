package com.autoservice.billingservice.dto.request;

import com.autoservice.billingservice.domain.enums.InvoiceItemType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddInvoiceItemRequest(

        @NotNull(
                message = "Loại chi phí không được để trống"
        )
        InvoiceItemType itemType,

        @Positive(
                message = "Mã tham chiếu phải lớn hơn 0"
        )
        Long referenceId,

        @NotBlank(
                message = "Tên chi phí không được để trống"
        )
        @Size(
                max = 200,
                message = "Tên chi phí không được vượt quá 200 ký tự"
        )
        String itemName,

        @Size(
                max = 1000,
                message = "Mô tả không được vượt quá 1000 ký tự"
        )
        String description,

        @NotNull(
                message = "Số lượng không được để trống"
        )
        @DecimalMin(
                value = "0.01",
                inclusive = true,
                message = "Số lượng phải lớn hơn 0"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "Số lượng không đúng định dạng"
        )
        BigDecimal quantity,

        @NotNull(
                message = "Đơn giá không được để trống"
        )
        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Đơn giá không được nhỏ hơn 0"
        )
        @Digits(
                integer = 13,
                fraction = 2,
                message = "Đơn giá không đúng định dạng"
        )
        BigDecimal unitPrice
) {
}