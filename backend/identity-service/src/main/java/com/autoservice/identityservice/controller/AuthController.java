package com.autoservice.identityservice.controller;

import com.autoservice.identityservice.common.ApiResponse;
import com.autoservice.identityservice.dto.request.RegisterRequest;
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
        UserResponse user = authService.register(request);

        ApiResponse<UserResponse> response =
                ApiResponse.success(
                        "Đăng ký tài khoản thành công. "
                                + "Tài khoản đang chờ kích hoạt.",
                        user
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}