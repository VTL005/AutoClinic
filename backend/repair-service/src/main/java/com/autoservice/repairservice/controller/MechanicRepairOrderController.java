package com.autoservice.repairservice.controller;

import com.autoservice.repairservice.common.ApiResponse;
import com.autoservice.repairservice.dto.response.RepairOrderResponse;
import com.autoservice.repairservice.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.autoservice.repairservice.dto.request.UpdateRepairOrderDetailsRequest;
import jakarta.validation.Valid;
import com.autoservice.repairservice.dto.request.UpdateRepairOrderStatusRequest;
import com.autoservice.repairservice.dto.request.UpdateRepairTaskStatusRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mechanic/repair-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MECHANIC')")
public class MechanicRepairOrderController {

    private final RepairOrderService repairOrderService;

    @GetMapping
    public ApiResponse<Page<RepairOrderResponse>>
    getAssignedRepairOrders(
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
                        .getMechanicRepairOrders(
                                getUserId(jwt),
                                pageable
                        );

        return ApiResponse.success(
                "Lấy danh sách phiếu được phân công thành công.",
                response
        );
    }

    @GetMapping("/{repairOrderId}")
    public ApiResponse<RepairOrderResponse>
    getAssignedRepairOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long repairOrderId
    ) {
        RepairOrderResponse response =
                repairOrderService
                        .getMechanicRepairOrder(
                                repairOrderId,
                                getUserId(jwt)
                        );

        return ApiResponse.success(
                "Lấy thông tin phiếu được phân công thành công.",
                response
        );
    }
    @PatchMapping("/{repairOrderId}/details")
    public ApiResponse<RepairOrderResponse>
    updateAssignedRepairOrderDetails(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long repairOrderId,
            @Valid @RequestBody
            UpdateRepairOrderDetailsRequest request
    ) {
        RepairOrderResponse response =
                repairOrderService
                        .updateMechanicRepairOrderDetails(
                                repairOrderId,
                                getUserId(jwt),
                                request
                        );

        return ApiResponse.success(
                "Cập nhật thông tin kỹ thuật thành công.",
                response
        );
    }
    @PatchMapping(
            "/{repairOrderId}/tasks/{taskId}/status"
    )
    public ApiResponse<RepairOrderResponse>
    updateAssignedRepairTaskStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long repairOrderId,
            @PathVariable Long taskId,
            @Valid @RequestBody
            UpdateRepairTaskStatusRequest request
    ) {
        RepairOrderResponse response =
                repairOrderService
                        .updateMechanicRepairTaskStatus(
                                repairOrderId,
                                taskId,
                                getUserId(jwt),
                                request
                        );

        return ApiResponse.success(
                "Cập nhật trạng thái công việc thành công.",
                response
        );
    }
    @PatchMapping("/{repairOrderId}/status")
    public ApiResponse<RepairOrderResponse>
    updateAssignedRepairOrderStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long repairOrderId,
            @Valid @RequestBody
            UpdateRepairOrderStatusRequest request
    ) {
        RepairOrderResponse response =
                repairOrderService
                        .updateMechanicRepairOrderStatus(
                                repairOrderId,
                                getUserId(jwt),
                                request
                        );

        return ApiResponse.success(
                "Cập nhật trạng thái phiếu sửa chữa thành công.",
                response
        );
    }
    private Long getUserId(
            Jwt jwt
    ) {
        return Long.valueOf(jwt.getSubject());
    }
}