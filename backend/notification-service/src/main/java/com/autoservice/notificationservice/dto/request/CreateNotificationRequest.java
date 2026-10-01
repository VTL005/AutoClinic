package com.autoservice.notificationservice.dto.request;

import com.autoservice.notificationservice.domain.enums.NotificationChannel;
import com.autoservice.notificationservice.domain.enums.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(

        @NotNull(
                message = "Mã người nhận không được để trống"
        )
        @Positive(
                message = "Mã người nhận phải lớn hơn 0"
        )
        Long recipientUserId,

        @Email(
                message = "Email người nhận không đúng định dạng"
        )
        @Size(
                max = 150,
                message = "Email không được vượt quá 150 ký tự"
        )
        String recipientEmail,

        @NotNull(
                message = "Loại thông báo không được để trống"
        )
        NotificationType notificationType,

        @NotNull(
                message = "Kênh thông báo không được để trống"
        )
        NotificationChannel channel,

        @NotBlank(
                message = "Tiêu đề không được để trống"
        )
        @Size(
                max = 200,
                message = "Tiêu đề không được vượt quá 200 ký tự"
        )
        String title,

        @NotBlank(
                message = "Nội dung không được để trống"
        )
        String content,

        @Size(
                max = 50,
                message = "Loại tham chiếu không được vượt quá 50 ký tự"
        )
        String referenceType,

        @Positive(
                message = "Mã tham chiếu phải lớn hơn 0"
        )
        Long referenceId,

        @Size(
                max = 50,
                message = "Tên service nguồn không được vượt quá 50 ký tự"
        )
        String sourceService,

        @Size(
                max = 150,
                message = "Khóa chống trùng không được vượt quá 150 ký tự"
        )
        String idempotencyKey
) {
}