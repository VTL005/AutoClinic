package com.autoservice.repairservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateRepairOrderRequest(

        @NotNull(
                message = "Mã lịch hẹn không được để trống"
        )
        @Positive(
                message = "Mã lịch hẹn phải lớn hơn 0"
        )
        Long bookingId,

        @PositiveOrZero(
                message = "Số kilomet không được nhỏ hơn 0"
        )
        Integer odometer,

        @Future(
                message = "Thời gian dự kiến hoàn thành "
                        + "phải nằm trong tương lai"
        )
        LocalDateTime estimatedCompletionAt,

        @Size(
                max = 2000,
                message = "Ghi chú kỹ thuật không được "
                        + "vượt quá 2000 ký tự"
        )
        String technicianNote
) {
}