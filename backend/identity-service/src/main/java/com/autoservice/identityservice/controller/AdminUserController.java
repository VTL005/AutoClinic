package com.autoservice.identityservice.controller;

import com.autoservice.identityservice.common.ApiResponse;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.dto.request.UpdateAccountStatusRequest;
import com.autoservice.identityservice.dto.request.AdminResetPasswordRequest;
import com.autoservice.identityservice.dto.response.AdminUserResponse;
import com.autoservice.identityservice.dto.response.PageResponse;
import com.autoservice.identityservice.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<AdminUserResponse>>
            > getUsers(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Role role,

            @RequestParam(required = false)
            AccountStatus accountStatus,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        PageResponse<AdminUserResponse> users =
                adminUserService.getUsers(
                        keyword,
                        role,
                        accountStatus,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách tài khoản thành công.",
                        users
                )
        );
    }
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserResponse>>
    getUserById(
            @PathVariable("userId")
            Long userId
    ) {
        AdminUserResponse user =
                adminUserService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin tài khoản thành công.",
                        user
                )
        );
    }
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<AdminUserResponse>>
    updateAccountStatus(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable
            Long userId,

            @Valid
            @RequestBody
            UpdateAccountStatusRequest request
    ) {
        Long currentAdminId =
                Long.valueOf(jwt.getSubject());

        AdminUserResponse user =
                adminUserService.updateAccountStatus(
                        currentAdminId,
                        userId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật trạng thái tài khoản thành công.",
                        user
                )
        );
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<AdminUserResponse>>
    resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody AdminResetPasswordRequest request
    ) {
        AdminUserResponse user =
                adminUserService.resetPassword(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đặt lại mật khẩu thành công.",
                        user
                )
        );
    }
}
