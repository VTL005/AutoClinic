package com.autoservice.identityservice.dto.request;

import com.autoservice.identityservice.domain.enums.SkillLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateMechanicRequest(

        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(
                min = 4,
                max = 50,
                message = "Tên đăng nhập phải có từ 4 đến 50 ký tự"
        )
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "Tên đăng nhập chứa ký tự không hợp lệ"
        )
        String username,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(
                min = 8,
                max = 72,
                message = "Mật khẩu phải có từ 8 đến 72 ký tự"
        )
        String password,

        @NotBlank(message = "Họ tên không được để trống")
        @Size(
                max = 100,
                message = "Họ tên không được vượt quá 100 ký tự"
        )
        String fullName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(
                regexp = com.autoservice.identityservice.service.PhoneNumbers.INPUT_PATTERN,
                message = "Số điện thoại không hợp lệ"
        )
        String phone,

        @Email(message = "Email không đúng định dạng")
        @Size(
                max = 150,
                message = "Email không được vượt quá 150 ký tự"
        )
        String email,

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
        BigDecimal hourlyRate

) {
}