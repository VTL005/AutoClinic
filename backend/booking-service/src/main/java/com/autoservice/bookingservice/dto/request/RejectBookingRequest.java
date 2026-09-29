package com.autoservice.bookingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectBookingRequest(

        @NotBlank(message = "Lý do từ chối không được để trống")
        @Size(
                max = 500,
                message = "Lý do từ chối không được vượt quá 500 ký tự"
        )
        String reason
) {
}