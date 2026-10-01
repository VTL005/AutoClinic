package com.autoservice.notificationservice.dto.response;

import com.autoservice.notificationservice.domain.enums.DeliveryStatus;
import com.autoservice.notificationservice.domain.enums.NotificationChannel;
import com.autoservice.notificationservice.domain.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long recipientUserId,
        String recipientEmail,
        NotificationType notificationType,
        NotificationChannel channel,
        String title,
        String content,
        String referenceType,
        Long referenceId,
        String sourceService,
        DeliveryStatus deliveryStatus,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime sentAt,
        LocalDateTime failedAt,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}