package com.autoservice.identityservice.controller;

import com.autoservice.identityservice.common.ApiResponse;
import com.autoservice.identityservice.dto.request.ChangePasswordRequest;
import com.autoservice.identityservice.dto.request.UpdateProfileRequest;
import com.autoservice.identityservice.dto.response.UserResponse;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>>
    getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = extractUserId(jwt);

        UserResponse user =
                userService.getCurrentUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin người dùng thành công.",
                        user
                )
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>>
    updateCurrentUser(
            @AuthenticationPrincipal Jwt jwt,

            @Valid
            @RequestBody
            UpdateProfileRequest request
    ) {
        Long userId = extractUserId(jwt);

        UserResponse user =
                userService.updateCurrentUser(
                        userId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật thông tin cá nhân thành công.",
                        user
                )
        );
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>>
    changePassword(
            @AuthenticationPrincipal Jwt jwt,

            @Valid
            @RequestBody
            ChangePasswordRequest request
    ) {
        Long userId = extractUserId(jwt);

        userService.changePassword(
                userId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đổi mật khẩu thành công. "
                                + "Vui lòng đăng nhập lại.",
                        null
                )
        );
    }

    private Long extractUserId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Access token không hợp lệ."
            );
        }
    }
}