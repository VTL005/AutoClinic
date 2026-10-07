package com.autoservice.identityservice.controller;

import com.autoservice.identityservice.common.ApiResponse;
import com.autoservice.identityservice.dto.request.ConfirmActivationCodeRequest;
import com.autoservice.identityservice.dto.request.RequestActivationCodeRequest;
import com.autoservice.identityservice.service.AccountActivationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/activation")
@RequiredArgsConstructor
public class AccountActivationController {
    private final AccountActivationService service;

    @PostMapping("/request")
    public ApiResponse<Void> request(@Valid @RequestBody RequestActivationCodeRequest request) {
        service.requestCode(request);
        return ApiResponse.success("Nếu tài khoản hợp lệ và đang chờ kích hoạt, "
                + "mã sẽ được gửi tới email đã đăng ký. Vui lòng kiểm tra hộp thư.");
    }

    @PostMapping("/confirm")
    public ApiResponse<Void> confirm(@Valid @RequestBody ConfirmActivationCodeRequest request) {
        service.confirm(request);
        return ApiResponse.success("Kích hoạt tài khoản thành công. Bạn có thể đăng nhập.");
    }
}
