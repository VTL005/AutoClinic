package com.autoservice.bookingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelBookingRequest(

        @NotBlank(message = "Lý do hủy không được để trống.")
        @Size(
                max = 500,
                message = "Lý do hủy không được vượt quá 500 ký tự."
        )
        String cancellationReason
) {
}