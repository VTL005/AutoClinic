package com.autoservice.identityservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(
                message = "Tên đăng nhập không được để trống"
        )
        @Size(
                min = 4,
                max = 50,
                message = "Tên đăng nhập phải từ 4 đến 50 ký tự"
        )
        @Pattern(
                regexp = "^[a-zA-Z0-9._]+$",
                message = "Tên đăng nhập chỉ được chứa chữ cái, "
                        + "chữ số, dấu chấm và dấu gạch dưới"
        )
        String username,

        @NotBlank(
                message = "Mật khẩu không được để trống"
        )
        @Size(
                min = 8,
                max = 72,
                message = "Mật khẩu phải từ 8 đến 72 ký tự"
        )
        @Pattern(
                regexp =
                        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Mật khẩu phải có chữ hoa, chữ thường "
                        + "và chữ số"
        )
        String password,

        @NotBlank(
                message = "Họ tên không được để trống"
        )
        @Size(
                min = 2,
                max = 100,
                message = "Họ tên phải từ 2 đến 100 ký tự"
        )
        String fullName,

        @NotBlank(
                message = "Số điện thoại không được để trống"
        )
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