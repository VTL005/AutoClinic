package com.autoservice.bookingservice.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateBookingRequest(

        @NotNull(message = "Phương tiện không được để trống.")
        @Positive(message = "Mã phương tiện không hợp lệ.")
        Long vehicleId,

        @NotBlank(message = "Loại dịch vụ không được để trống.")
        @Size(
                max = 100,
                message = "Loại dịch vụ không được vượt quá 100 ký tự."
        )
        String serviceType,

        @NotNull(message = "Ngày đặt lịch không được để trống.")
        @FutureOrPresent(
                message = "Ngày đặt lịch không được nằm trong quá khứ."
        )
        LocalDate requestedDate,

        @NotNull(message = "Giờ đặt lịch không được để trống.")
        LocalTime requestedTime,

        @Size(
                max = 1000,
                message = "Ghi chú không được vượt quá 1000 ký tự."
        )
        String customerNote
) {
}