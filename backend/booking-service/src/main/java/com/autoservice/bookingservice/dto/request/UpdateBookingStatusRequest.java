package com.autoservice.bookingservice.dto.request;

import com.autoservice.bookingservice.domain.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBookingStatusRequest(

        @NotNull(message = "Trạng thái mới không được để trống")
        BookingStatus status,

        @Size(
                max = 1000,
                message = "Ghi chú nội bộ không được vượt quá 1000 ký tự"
        )
        String internalNote
) {
}