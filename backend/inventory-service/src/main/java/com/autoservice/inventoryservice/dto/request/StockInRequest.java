package com.autoservice.inventoryservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockInRequest(

        @NotNull(
                message = "Số lượng nhập không được để trống"
        )
        @Positive(
                message = "Số lượng nhập phải lớn hơn 0"
        )
        Integer quantity,

        @Size(
                max = 50,
                message = "Loại tham chiếu không được vượt quá 50 ký tự"
        )
        String referenceType,

        @Positive(
                message = "Mã tham chiếu phải lớn hơn 0"
        )
        Long referenceId,

        @Size(
                max = 1000,
                message = "Ghi chú không được vượt quá 1000 ký tự"
        )
        String note
) {
}