package com.autoservice.inventoryservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AdjustStockRequest(

        @NotNull(
                message = "Số lượng tồn kho mới không được để trống"
        )
        @PositiveOrZero(
                message = "Số lượng tồn kho mới không được nhỏ hơn 0"
        )
        Integer newQuantity,

        @Size(
                max = 1000,
                message = "Lý do điều chỉnh không được vượt quá 1000 ký tự"
        )
        String note
) {
}