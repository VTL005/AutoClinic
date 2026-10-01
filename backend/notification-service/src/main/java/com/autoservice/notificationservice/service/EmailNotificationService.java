package com.autoservice.notificationservice.service;

import com.autoservice.notificationservice.domain.entity.Notification;
import com.autoservice.notificationservice.domain.entity.NotificationDeliveryAttempt;
import com.autoservice.notificationservice.domain.enums.DeliveryStatus;
import com.autoservice.notificationservice.domain.enums.NotificationChannel;
import com.autoservice.notificationservice.repository.NotificationDeliveryAttemptRepository;
import com.autoservice.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    private final NotificationRepository
            notificationRepository;

    private final NotificationDeliveryAttemptRepository
            deliveryAttemptRepository;

    @Transactional
    public Notification send(
            Notification notification
    ) {
        validateNotification(notification);

        int attemptNumber =
                Math.toIntExact(
                        deliveryAttemptRepository
                                .countByNotificationId(
                                        notification.getId()
                                )
                                + 1
                );

        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(
                    notification.getRecipientEmail()
            );

            message.setSubject(
                    notification.getTitle()
            );

            message.setText(
                    notification.getContent()
            );

            mailSender.send(message);

            LocalDateTime sentAt =
                    LocalDateTime.now();

            notification.setDeliveryStatus(
                    DeliveryStatus.SENT
            );
            notification.setSentAt(sentAt);
            notification.setFailedAt(null);
            notification.setFailureReason(null);

            Notification savedNotification =
                    notificationRepository.save(
                            notification
                    );

            saveAttempt(
                    savedNotification,
                    attemptNumber,
                    DeliveryStatus.SENT,
                    null
            );

            return savedNotification;
        } catch (MailException exception) {
            LocalDateTime failedAt =
                    LocalDateTime.now();

            String failureReason =
                    truncate(
                            exception.getMessage(),
                            500
                    );

            notification.setDeliveryStatus(
                    DeliveryStatus.FAILED
            );
            notification.setSentAt(null);
            notification.setFailedAt(failedAt);
            notification.setFailureReason(
                    failureReason
            );

            Notification savedNotification =
                    notificationRepository.save(
                            notification
                    );

            saveAttempt(
                    savedNotification,
                    attemptNumber,
                    DeliveryStatus.FAILED,
                    failureReason
            );

            return savedNotification;
        }
    }

    private void validateNotification(
            Notification notification
    ) {
        if (notification == null
                || notification.getId() == null) {

            throw new IllegalArgumentException(
                    "Thông báo phải được lưu trước khi gửi email."
            );
        }

        if (notification.getChannel()
                != NotificationChannel.EMAIL) {

            throw new IllegalArgumentException(
                    "Chỉ thông báo kênh EMAIL mới được gửi email."
            );
        }

        if (notification.getRecipientEmail() == null
                || notification.getRecipientEmail()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Email người nhận không được để trống."
            );
        }
    }

    private void saveAttempt(
            Notification notification,
            int attemptNumber,
            DeliveryStatus status,
            String errorMessage
    ) {
        NotificationDeliveryAttempt attempt =
                NotificationDeliveryAttempt
                        .builder()
                        .notification(notification)
                        .attemptNumber(
                                attemptNumber
                        )
                        .status(status)
                        .providerMessageId(null)
                        .errorMessage(errorMessage)
                        .build();

        deliveryAttemptRepository.save(attempt);
    }

    private String truncate(
            String value,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            return "Không thể gửi email.";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}