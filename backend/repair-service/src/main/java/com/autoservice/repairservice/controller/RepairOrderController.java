package com.autoservice.repairservice.controller;

import com.autoservice.repairservice.common.ApiResponse;
import com.autoservice.repairservice.dto.request.CreateRepairOrderRequest;
import com.autoservice.repairservice.dto.response.RepairOrderResponse;
import com.autoservice.repairservice.service.RepairOrderService;
import com.autoservice.repairservice.dto.request.UpdateRepairOrderStatusRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import com.autoservice.repairservice.dto.request.UpdateRepairOrderDetailsRequest;
import com.autoservice.repairservice.domain.enums.RepairOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestParam;
import com.autoservice.repairservice.dto.request.UpdateRepairTaskStatusRequest;
import jakarta.validation.Valid;
import com.autoservice.repairservice.dto.request.AddRepairTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/repair-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<RepairOrderResponse>>
    createRepairOrder(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,
            @Valid @RequestBody
            CreateRepairOrderRequest request
    ) {
        RepairOrderResponse response =
                repairOrderService.createRepairOrder(
                        getUserId(jwt),
                        authorizationHeader,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo phiếu sửa chữa thành công.",
                                response
                        )
                );
    }
    @GetMapping
    public ApiResponse<Page<RepairOrderResponse>>
    getRepairOrders(
            @RequestParam(required = false)
            RepairOrderStatus status,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<RepairOrderResponse> response =
                repairOrderService.getRepairOrders(
                        status,
                        pageable
                );

        return ApiResponse.success(
                "Lấy danh sách phiếu sửa chữa thành công.",
                response
        );
    }

    @GetMapping("/{repairOrderId}")
    public ApiResponse<RepairOrderResponse>
    getRepairOrder(
            @PathVariable Long repairOrderId
    ) {
        RepairOrderResponse response =
                repairOrderService.getRepairOrder(
                        repairOrderId
                );

        return ApiResponse.success(
                "Lấy thông tin phiếu sửa chữa thành công.",
                response
        );
    }
    @PatchMapping("/{repairOrderId}/details")
    public ApiResponse<RepairOrderResponse>
    updateRepairOrderDetails(
            @PathVariable Long repairOrderId,
            @Valid @RequestBody
            UpdateRepairOrderDetailsRequest request
    ) {
        RepairOrderResponse response =
                repairOrderService.updateRepairOrderDetails(
                        repairOrderId,
                        request
                );

        return ApiResponse.success(
                "Cập nhật thông tin phiếu sửa chữa thành công.",
                response
        );
    }
    @PostMapping("/{repairOrderId}/tasks")
    public ResponseEntity<ApiResponse<RepairOrderResponse>>
    addRepairTask(
            @PathVariable Long repairOrderId,
            @Valid @RequestBody
            AddRepairTaskRequest request
    ) {
        RepairOrderResponse response =
                repairOrderService.addRepairTask(
                        repairOrderId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Thêm công việc sửa chữa thành công.",
                                response
                        )
                );
    }
    @PatchMapping("/{repairOrderId}/status")
    public ApiResponse<RepairOrderResponse>
    updateRepairOrderStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long repairOrderId,
            @Valid @RequestBody
            UpdateRepairOrderStatusRequest request
    ) {
        RepairOrderResponse response =
                repairOrderService.updateRepairOrderStatus(
                        repairOrderId,
                        getUserId(jwt),
                        request
                );

        return ApiResponse.success(
                "Cập nhật trạng thái phiếu sửa chữa thành công.",
                response
        );
    }
    @PatchMapping("/{repairOrderId}/tasks/{taskId}/status")
    public ApiResponse<RepairOrderResponse> updateRepairTaskStatus(
            @PathVariable Long repairOrderId,
            @PathVariable Long taskId,
            @Valid @RequestBody
            UpdateRepairTaskStatusRequest request
    ) {
        RepairOrderResponse response =
                repairOrderService.updateRepairTaskStatus(
                        repairOrderId,
                        taskId,
                        request
                );

        return ApiResponse.success(
                "Cập nhật trạng thái công việc thành công.",
                response
        );
    }
    private Long getUserId(
            Jwt jwt
    ) {
        return Long.valueOf(jwt.getSubject());
    }
}