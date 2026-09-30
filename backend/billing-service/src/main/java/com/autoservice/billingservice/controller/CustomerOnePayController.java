package com.autoservice.billingservice.controller;

import com.autoservice.billingservice.common.ApiResponse;
import com.autoservice.billingservice.dto.request.InitiateOnlinePaymentRequest;
import com.autoservice.billingservice.dto.response.PaymentResponse;
import com.autoservice.billingservice.service.OnePayPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOnePayController {

    private final OnePayPaymentService onePayPaymentService;

    @PostMapping("/invoices/{invoiceId}/onepay")
    public ResponseEntity<ApiResponse<PaymentResponse>>
    createOnePayPayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long invoiceId,
            HttpServletRequest httpRequest,
            @Valid @RequestBody
            InitiateOnlinePaymentRequest request
    ) {
        PaymentResponse response =
                onePayPaymentService.createPaymentUrl(
                        invoiceId,
                        getUserId(jwt),
                        getClientIp(httpRequest),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo liên kết thanh toán OnePay thành công.",
                                response
                        )
                );
    }

    private Long getUserId(
            Jwt jwt
    ) {
        return Long.valueOf(jwt.getSubject());
    }

    private String getClientIp(
            HttpServletRequest request
    ) {
        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {
            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return request.getRemoteAddr();
    }
}