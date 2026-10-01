package com.autoservice.notificationservice.service;

import com.autoservice.notificationservice.domain.entity.Notification;
import com.autoservice.notificationservice.domain.enums.DeliveryStatus;
import com.autoservice.notificationservice.domain.enums.NotificationChannel;
import com.autoservice.notificationservice.dto.request.CreateNotificationRequest;
import com.autoservice.notificationservice.dto.response.NotificationResponse;
import com.autoservice.notificationservice.dto.response.PageResponse;
import com.autoservice.notificationservice.dto.response.UnreadCountResponse;
import com.autoservice.notificationservice.exception.BusinessException;
import com.autoservice.notificationservice.exception.ErrorCode;
import com.autoservice.notificationservice.exception.ResourceNotFoundException;
import com.autoservice.notificationservice.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationRepository notificationRepository;
    private final EmailNotificationService
            emailNotificationService;

    @Transactional
    public NotificationResponse createNotification(
            CreateNotificationRequest request
    ) {
        validateEmailChannel(request);

        String idempotencyKey =
                normalize(request.idempotencyKey());

        if (idempotencyKey != null) {
            Notification existingNotification =
                    notificationRepository
                            .findByIdempotencyKey(
                                    idempotencyKey
                            )
                            .orElse(null);

            if (existingNotification != null) {
                return toResponse(existingNotification);
            }
        }

        Notification notification =
                Notification.builder()
                        .recipientUserId(
                                request.recipientUserId()
                        )
                        .recipientEmail(
                                normalize(request.recipientEmail())
                        )
                        .notificationType(
                                request.notificationType()
                        )
                        .channel(request.channel())
                        .title(request.title().trim())
                        .content(request.content().trim())
                        .referenceType(
                                normalize(request.referenceType())
                        )
                        .referenceId(request.referenceId())
                        .sourceService(
                                normalize(request.sourceService())
                        )
                        .idempotencyKey(idempotencyKey)
                        .deliveryStatus(
                                initialDeliveryStatus(
                                        request.channel()
                                )
                        )
                        .build();

        Notification savedNotification =
                notificationRepository.saveAndFlush(
                        notification
                );

        if (savedNotification.getChannel()
                == NotificationChannel.EMAIL) {

            savedNotification =
                    emailNotificationService.send(
                            savedNotification
                    );
        }

        return toResponse(savedNotification);
    }
    @Transactional
    public NotificationResponse retryEmail(
            Long notificationId
    ) {
        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        ErrorCode.NOTIFICATION_NOT_FOUND,
                                        "Không tìm thấy thông báo."
                                )
                        );

        if (notification.getChannel()
                != NotificationChannel.EMAIL) {

            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Chỉ thông báo EMAIL mới được gửi lại."
            );
        }

        if (notification.getDeliveryStatus()
                == DeliveryStatus.SENT) {

            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Email đã được gửi thành công, "
                            + "không thể gửi lại."
            );
        }

        Notification retriedNotification =
                emailNotificationService.send(
                        notification
                );

        return toResponse(retriedNotification);
    }
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse>
    getNotifications(
            Long recipientUserId,
            Pageable pageable
    ) {
        validatePageSize(pageable);

        Page<NotificationResponse> result =
                notificationRepository
                        .findAllByRecipientUserId(
                                recipientUserId,
                                pageable
                        )
                        .map(this::toResponse);

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotification(
            Long recipientUserId,
            Long notificationId
    ) {
        Notification notification =
                requireOwnedNotification(
                        recipientUserId,
                        notificationId
                );

        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(
            Long recipientUserId
    ) {
        long unreadCount =
                notificationRepository
                        .countByRecipientUserIdAndReadAtIsNull(
                                recipientUserId
                        );

        return new UnreadCountResponse(unreadCount);
    }

    @Transactional
    public NotificationResponse markAsRead(
            Long recipientUserId,
            Long notificationId
    ) {
        Notification notification =
                requireOwnedNotification(
                        recipientUserId,
                        notificationId
                );

        if (notification.getReadAt() == null) {
            notification.setReadAt(
                    LocalDateTime.now()
            );

            notification =
                    notificationRepository.saveAndFlush(
                            notification
                    );
        }

        return toResponse(notification);
    }

    @Transactional
    public int markAllAsRead(
            Long recipientUserId
    ) {
        return notificationRepository.markAllAsRead(
                recipientUserId,
                LocalDateTime.now()
        );
    }

    private Notification requireOwnedNotification(
            Long recipientUserId,
            Long notificationId
    ) {
        return notificationRepository
                .findByIdAndRecipientUserId(
                        notificationId,
                        recipientUserId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorCode.NOTIFICATION_NOT_FOUND,
                                "Không tìm thấy thông báo."
                        )
                );
    }

    private void validateEmailChannel(
            CreateNotificationRequest request
    ) {
        if (request.channel()
                == NotificationChannel.EMAIL
                && normalize(request.recipientEmail())
                == null) {

            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Email người nhận là bắt buộc "
                            + "khi gửi thông báo qua email."
            );
        }
    }

    private void validatePageSize(
            Pageable pageable
    ) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Số phần tử mỗi trang "
                            + "không được vượt quá 100."
            );
        }
    }

    private DeliveryStatus initialDeliveryStatus(
            NotificationChannel channel
    ) {
        if (channel == NotificationChannel.IN_APP) {
            return DeliveryStatus.SENT;
        }

        return DeliveryStatus.PENDING;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getRecipientEmail(),
                notification.getNotificationType(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getContent(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getSourceService(),
                notification.getDeliveryStatus(),
                notification.getReadAt() != null,
                notification.getReadAt(),
                notification.getSentAt(),
                notification.getFailedAt(),
                notification.getFailureReason(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}