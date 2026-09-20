package com.autoservice.identityservice.controller;

import com.autoservice.identityservice.common.ApiResponse;
import com.autoservice.identityservice.dto.request.LoginRequest;
import com.autoservice.identityservice.dto.request.RefreshTokenRequest;
import com.autoservice.identityservice.dto.request.RegisterRequest;
import com.autoservice.identityservice.dto.response.AuthTokenResponse;
import com.autoservice.identityservice.dto.response.UserResponse;
import com.autoservice.identityservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse user =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Đăng ký tài khoản thành công. "
                                        + "Tài khoản đang chờ kích hoạt.",
                                user
                        )
                );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthTokenResponse tokenResponse =
                authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đăng nhập thành công.",
                        tokenResponse
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthTokenResponse tokenResponse =
                authService.refresh(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Làm mới token thành công.",
                        tokenResponse
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đăng xuất thành công."
                )
        );
    }
}