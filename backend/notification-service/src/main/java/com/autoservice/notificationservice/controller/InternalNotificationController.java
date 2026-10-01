package com.autoservice.notificationservice.controller;

import com.autoservice.notificationservice.common.ApiResponse;
import com.autoservice.notificationservice.dto.request.CreateNotificationRequest;
import com.autoservice.notificationservice.dto.response.NotificationResponse;
import com.autoservice.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>>
    createNotification(
            @Valid
            @RequestBody
            CreateNotificationRequest request
    ) {
        NotificationResponse notification =
                notificationService.createNotification(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo thông báo thành công.",
                                notification
                        )
                );
    }
    @PostMapping("/{notificationId}/retry")
    public ResponseEntity<ApiResponse<NotificationResponse>>
    retryEmail(
            @PathVariable
            Long notificationId
    ) {
        NotificationResponse notification =
                notificationService.retryEmail(
                        notificationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Gửi lại email hoàn tất.",
                        notification
                )
        );
    }
}