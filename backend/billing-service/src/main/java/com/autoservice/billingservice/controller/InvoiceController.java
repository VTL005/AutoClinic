package com.autoservice.billingservice.controller;

import com.autoservice.billingservice.common.ApiResponse;
import com.autoservice.billingservice.domain.enums.InvoiceStatus;
import com.autoservice.billingservice.dto.request.AddInvoiceItemRequest;
import com.autoservice.billingservice.dto.request.CreateInvoiceRequest;
import com.autoservice.billingservice.dto.response.InvoiceResponse;
import com.autoservice.billingservice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>>
    createInvoice(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,
            @Valid @RequestBody
            CreateInvoiceRequest request
    ) {
        InvoiceResponse response =
                invoiceService.createInvoice(
                        getUserId(jwt),
                        authorizationHeader,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo hóa đơn thành công.",
                                response
                        )
                );
    }

    @GetMapping
    public ApiResponse<Page<InvoiceResponse>>
    getInvoices(
            @RequestParam(required = false)
            InvoiceStatus status,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<InvoiceResponse> response =
                invoiceService.getInvoices(
                        status,
                        pageable
                );

        return ApiResponse.success(
                "Lấy danh sách hóa đơn thành công.",
                response
        );
    }

    @GetMapping("/{invoiceId}")
    public ApiResponse<InvoiceResponse>
    getInvoice(
            @PathVariable Long invoiceId
    ) {
        InvoiceResponse response =
                invoiceService.getInvoice(
                        invoiceId
                );

        return ApiResponse.success(
                "Lấy thông tin hóa đơn thành công.",
                response
        );
    }

    @PostMapping("/{invoiceId}/items")
    public ResponseEntity<ApiResponse<InvoiceResponse>>
    addInvoiceItem(
            @PathVariable Long invoiceId,
            @Valid @RequestBody
            AddInvoiceItemRequest request
    ) {
        InvoiceResponse response =
                invoiceService.addInvoiceItem(
                        invoiceId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Thêm chi phí vào hóa đơn thành công.",
                                response
                        )
                );
    }

    @PatchMapping("/{invoiceId}/issue")
    public ApiResponse<InvoiceResponse>
    issueInvoice(
            @PathVariable Long invoiceId
    ) {
        InvoiceResponse response =
                invoiceService.issueInvoice(
                        invoiceId
                );

        return ApiResponse.success(
                "Phát hành hóa đơn thành công.",
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