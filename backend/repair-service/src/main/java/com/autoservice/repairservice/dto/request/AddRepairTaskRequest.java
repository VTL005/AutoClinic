package com.autoservice.repairservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddRepairTaskRequest(

        @NotBlank(
                message = "Tên công việc không được để trống"
        )
        @Size(
                max = 200,
                message = "Tên công việc không được vượt quá "
                        + "200 ký tự"
        )
        String taskName,

        @Size(
                max = 1000,
                message = "Mô tả công việc không được vượt quá "
                        + "1000 ký tự"
        )
        String description,

        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Số giờ công không được nhỏ hơn 0"
        )
        @Digits(
                integer = 4,
                fraction = 2,
                message = "Số giờ công không đúng định dạng"
        )
        BigDecimal laborHours,

        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Chi phí nhân công không được nhỏ hơn 0"
        )
        @Digits(
                integer = 13,
                fraction = 2,
                message = "Chi phí nhân công không đúng định dạng"
        )
        BigDecimal laborCost
) {
}