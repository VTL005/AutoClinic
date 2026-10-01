package com.autoservice.notificationservice.controller;

import com.autoservice.notificationservice.common.ApiResponse;
import com.autoservice.notificationservice.dto.response.MarkAllReadResponse;
import com.autoservice.notificationservice.dto.response.NotificationResponse;
import com.autoservice.notificationservice.dto.response.PageResponse;
import com.autoservice.notificationservice.dto.response.UnreadCountResponse;
import com.autoservice.notificationservice.exception.BusinessException;
import com.autoservice.notificationservice.exception.ErrorCode;
import com.autoservice.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/notifications")
@RequiredArgsConstructor
public class CustomerNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<NotificationResponse>>
            > getNotifications(
            @AuthenticationPrincipal Jwt jwt,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Long userId = extractUserId(jwt);

        PageResponse<NotificationResponse> notifications =
                notificationService.getNotifications(
                        userId,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách thông báo thành công.",
                        notifications
                )
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<
            ApiResponse<UnreadCountResponse>
            > getUnreadCount(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = extractUserId(jwt);

        UnreadCountResponse result =
                notificationService.getUnreadCount(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy số thông báo chưa đọc thành công.",
                        result
                )
        );
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<
            ApiResponse<NotificationResponse>
            > getNotification(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            Long notificationId
    ) {
        Long userId = extractUserId(jwt);

        NotificationResponse notification =
                notificationService.getNotification(
                        userId,
                        notificationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy chi tiết thông báo thành công.",
                        notification
                )
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<
            ApiResponse<NotificationResponse>
            > markAsRead(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            Long notificationId
    ) {
        Long userId = extractUserId(jwt);

        NotificationResponse notification =
                notificationService.markAsRead(
                        userId,
                        notificationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đánh dấu thông báo đã đọc thành công.",
                        notification
                )
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<
            ApiResponse<MarkAllReadResponse>
            > markAllAsRead(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = extractUserId(jwt);

        int updatedCount =
                notificationService.markAllAsRead(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đánh dấu tất cả thông báo đã đọc thành công.",
                        new MarkAllReadResponse(updatedCount)
                )
        );
    }

    private Long extractUserId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "Access token không hợp lệ."
            );
        }
    }
}