package com.autoservice.bookingservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ConfirmBookingRequest(

        @NotNull(message = "Mã thợ máy không được để trống")
        @Positive(message = "Mã thợ máy phải lớn hơn 0")
        Long mechanicUserId,

        @NotNull(message = "Thời gian bắt đầu không được để trống")
        @Future(message = "Thời gian bắt đầu phải ở tương lai")
        LocalDateTime scheduledStartAt,

        @NotNull(message = "Thời gian kết thúc không được để trống")
        @Future(message = "Thời gian kết thúc phải ở tương lai")
        LocalDateTime scheduledEndAt,

        @Size(max = 1000, message = "Ghi chú nội bộ không được vượt quá 1000 ký tự")
        String internalNote
) {
}