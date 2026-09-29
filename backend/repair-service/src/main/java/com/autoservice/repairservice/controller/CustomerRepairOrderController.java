package com.autoservice.repairservice.controller;

import com.autoservice.repairservice.common.ApiResponse;
import com.autoservice.repairservice.dto.response.RepairOrderResponse;
import com.autoservice.repairservice.service.RepairOrderService;
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
@RequestMapping("/api/v1/customer/repair-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerRepairOrderController {

    private final RepairOrderService repairOrderService;

    @GetMapping
    public ApiResponse<Page<RepairOrderResponse>>
    getCustomerRepairOrders(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<RepairOrderResponse> response =
                repairOrderService
                        .getCustomerRepairOrders(
                                getUserId(jwt),
                                pageable
                        );

        return ApiResponse.success(
                "Lấy danh sách phiếu sửa chữa thành công.",
                response
        );
    }

    @GetMapping("/{repairOrderId}")
    public ApiResponse<RepairOrderResponse>
    getCustomerRepairOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long repairOrderId
    ) {
        RepairOrderResponse response =
                repairOrderService
                        .getCustomerRepairOrder(
                                repairOrderId,
                                getUserId(jwt)
                        );

        return ApiResponse.success(
                "Lấy thông tin phiếu sửa chữa thành công.",
                response
        );
    }

    private Long getUserId(
            Jwt jwt
    ) {
        return Long.valueOf(jwt.getSubject());
    }
}