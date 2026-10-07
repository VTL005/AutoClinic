package com.autoservice.identityservice.controller;
import com.autoservice.identityservice.common.ApiResponse;
import com.autoservice.identityservice.dto.request.CreateWalkInCustomerRequest;
import com.autoservice.identityservice.service.WalkInCustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users/customers/walk-in")
@PreAuthorize("hasRole('ADMIN')")
public class WalkInCustomerController {
    private final WalkInCustomerService service;
    @PostMapping public ApiResponse<WalkInCustomerService.Customer> create(@Valid @RequestBody CreateWalkInCustomerRequest request) {
        return ApiResponse.success("Tiếp nhận thông tin khách hàng thành công.",service.create(request));
    }
}
