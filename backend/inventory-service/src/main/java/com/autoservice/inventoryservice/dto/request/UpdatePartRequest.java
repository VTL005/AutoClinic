package com.autoservice.inventoryservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdatePartRequest(

        @NotBlank(
                message = "Tên phụ tùng không được để trống"
        )
        @Size(
                max = 200,
                message = "Tên phụ tùng không được vượt quá 200 ký tự"
        )
        String name,

        @Size(
                max = 1000,
                message = "Mô tả không được vượt quá 1000 ký tự"
        )
        String description,

        @Size(
                max = 150,
                message = "Nhà sản xuất không được vượt quá 150 ký tự"
        )
        String manufacturer,

        @NotBlank(
                message = "Đơn vị tính không được để trống"
        )
        @Size(
                max = 30,
                message = "Đơn vị tính không được vượt quá 30 ký tự"
        )
        String unit,

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
        BigDecimal unitPrice,

        @NotNull(
                message = "Mức tồn kho tối thiểu không được để trống"
        )
        @PositiveOrZero(
                message = "Mức tồn kho tối thiểu không được nhỏ hơn 0"
        )
        Integer minimumStock
) {
}