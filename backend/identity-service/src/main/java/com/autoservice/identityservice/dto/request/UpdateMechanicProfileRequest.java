package com.autoservice.identityservice.dto.request;

import com.autoservice.identityservice.domain.enums.EmploymentStatus;
import com.autoservice.identityservice.domain.enums.SkillLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateMechanicProfileRequest(

        @NotNull(message = "Cấp độ kỹ thuật viên không được để trống")
        SkillLevel skillLevel,

        @Size(
                max = 100,
                message = "Chuyên môn không được vượt quá 100 ký tự"
        )
        String specialization,

        @NotNull(message = "Đơn giá công không được để trống")
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Đơn giá công không được âm"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "Đơn giá công không đúng định dạng"
        )
        BigDecimal hourlyRate,

        @NotNull(message = "Trạng thái làm việc không được để trống")
        EmploymentStatus employmentStatus

) {
}