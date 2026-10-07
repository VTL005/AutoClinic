package com.autoservice.identityservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfirmActivationCodeRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Pattern(regexp = "[0-9]{6}", message = "Mã kích hoạt phải gồm 6 chữ số.") String code
) {}
