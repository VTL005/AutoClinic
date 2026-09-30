package com.autoservice.billingservice.controller;

import com.autoservice.billingservice.common.ApiResponse;
import com.autoservice.billingservice.dto.request.ConfirmCashPaymentRequest;
import com.autoservice.billingservice.dto.response.InvoiceResponse;
import com.autoservice.billingservice.dto.response.PaymentResponse;
import com.autoservice.billingservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{invoiceId}/payments/cash")
    public ApiResponse<InvoiceResponse>
    confirmCashPayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long invoiceId,
            @Valid @RequestBody
            ConfirmCashPaymentRequest request
    ) {
        InvoiceResponse response =
                paymentService.confirmCashPayment(
                        invoiceId,
                        getUserId(jwt),
                        request
                );

        return ApiResponse.success(
                "Xác nhận thanh toán tiền mặt thành công.",
                response
        );
    }

    @GetMapping("/{invoiceId}/payments")
    public ApiResponse<Page<PaymentResponse>>
    getPayments(
            @PathVariable Long invoiceId,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<PaymentResponse> response =
                paymentService.getPayments(
                        invoiceId,
                        pageable
                );

        return ApiResponse.success(
                "Lấy lịch sử thanh toán thành công.",
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