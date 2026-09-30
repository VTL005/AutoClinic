package com.autoservice.billingservice.controller;

import com.autoservice.billingservice.common.ApiResponse;
import com.autoservice.billingservice.dto.response.InvoiceResponse;
import com.autoservice.billingservice.service.CustomerInvoiceService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerInvoiceController {

    private final CustomerInvoiceService
            customerInvoiceService;

    @GetMapping
    public ApiResponse<Page<InvoiceResponse>>
    getCustomerInvoices(
            @AuthenticationPrincipal Jwt jwt,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<InvoiceResponse> response =
                customerInvoiceService
                        .getCustomerInvoices(
                                getUserId(jwt),
                                pageable
                        );

        return ApiResponse.success(
                "Lấy danh sách hóa đơn thành công.",
                response
        );
    }

    @GetMapping("/{invoiceId}")
    public ApiResponse<InvoiceResponse>
    getCustomerInvoice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long invoiceId
    ) {
        InvoiceResponse response =
                customerInvoiceService
                        .getCustomerInvoice(
                                invoiceId,
                                getUserId(jwt)
                        );

        return ApiResponse.success(
                "Lấy thông tin hóa đơn thành công.",
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