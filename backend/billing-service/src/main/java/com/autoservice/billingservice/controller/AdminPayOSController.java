package com.autoservice.billingservice.controller;

import com.autoservice.billingservice.common.ApiResponse;
import com.autoservice.billingservice.dto.request.RegisterPayOSWebhookRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;

@RestController
@RequestMapping("/api/v1/admin/payments/payos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPayOSController {

    private final PayOS payOS;

    @PostMapping("/webhook/register")
    public ApiResponse<String> registerWebhook(
            @Valid @RequestBody
            RegisterPayOSWebhookRequest request
    ) {
        payOS.webhooks()
                .confirm(
                        request.webhookUrl()
                );

        return ApiResponse.success(
                "Đăng ký webhook payOS thành công.",
                request.webhookUrl()
        );
    }
}