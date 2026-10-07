package com.autoservice.identityservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestActivationCodeRequest(
        @NotBlank @Size(max = 50) String username
) {}
