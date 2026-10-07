package com.autoservice.identityservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;

public record LoginRequest(

        @NotBlank(message = "Tên đăng nhập, email hoặc số điện thoại không được để trống.")
        @Size(max = 150, message = "Thông tin đăng nhập không được vượt quá 150 ký tự.")
        @JsonAlias("identifier")
        String username,

        @NotBlank(message = "Mật khẩu không được để trống.")
        String password

) {
}
