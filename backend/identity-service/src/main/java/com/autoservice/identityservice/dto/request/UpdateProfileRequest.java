package com.autoservice.identityservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @Size(
                min = 2,
                max = 100,
                message = "Họ tên phải từ 2 đến 100 ký tự"
        )
        String fullName,

        @Pattern(
                regexp = "^\\+?[1-9]\\d{7,14}$",
                message = "Số điện thoại không đúng định dạng"
        )
        String phone,

        @Email(
                message = "Email không đúng định dạng"
        )
        @Size(
                max = 150,
                message = "Email không được vượt quá 150 ký tự"
        )
        String email
) {
}