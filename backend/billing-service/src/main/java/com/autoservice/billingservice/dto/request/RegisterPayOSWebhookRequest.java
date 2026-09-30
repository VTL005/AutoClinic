package com.autoservice.billingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterPayOSWebhookRequest(

        @NotBlank(
                message = "URL webhook không được để trống"
        )
        @Pattern(
                regexp = "^https://.+",
                message = "URL webhook phải sử dụng HTTPS"
        )
        String webhookUrl
) {
}