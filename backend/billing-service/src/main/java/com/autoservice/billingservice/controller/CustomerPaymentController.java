package com.autoservice.billingservice.controller;

import com.autoservice.billingservice.common.ApiResponse;
import com.autoservice.billingservice.dto.request.InitiateOnlinePaymentRequest;
import com.autoservice.billingservice.dto.response.PaymentResponse;
import com.autoservice.billingservice.service.CustomerPaymentService;
import com.autoservice.billingservice.service.PayOSPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerPaymentController {

    private final CustomerPaymentService
            customerPaymentService;

    private final PayOSPaymentService
            payOSPaymentService;

    @PostMapping(
            "/invoices/{invoiceId}/payos"
    )
    public ResponseEntity<
            ApiResponse<PaymentResponse>
            > createPayOSPayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long invoiceId,
            @Valid @RequestBody
            InitiateOnlinePaymentRequest request
    ) {
        PaymentResponse response =
                payOSPaymentService
                        .createPaymentLink(
                                invoiceId,
                                getUserId(jwt),
                                request
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo liên kết thanh toán "
                                        + "payOS thành công.",
                                response
                        )
                );
    }

    @GetMapping("/{paymentId}/status")
    public ApiResponse<PaymentResponse>
    getPaymentStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long paymentId
    ) {
        PaymentResponse response =
                customerPaymentService
                        .getPaymentStatus(
                                paymentId,
                                getUserId(jwt)
                        );

        return ApiResponse.success(
                "Lấy trạng thái thanh toán thành công.",
                response
        );
    }

    private Long getUserId(
            Jwt jwt
    ) {
        return Long.valueOf(
                jwt.getSubject()
        );
    }
}