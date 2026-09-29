package com.autoservice.repairservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateRepairOrderDetailsRequest(

        @Size(
                max = 2000,
                message = "Kết quả chẩn đoán không được vượt quá 2000 ký tự"
        )
        String diagnosis,

        @Size(
                max = 2000,
                message = "Ghi chú kỹ thuật không được vượt quá 2000 ký tự"
        )
        String technicianNote,

        @PositiveOrZero(
                message = "Số kilomet không được nhỏ hơn 0"
        )
        Integer odometer,

        @Future(
                message = "Thời gian dự kiến hoàn thành phải ở tương lai"
        )
        LocalDateTime estimatedCompletionAt
) {
}