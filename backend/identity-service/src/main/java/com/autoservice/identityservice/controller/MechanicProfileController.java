package com.autoservice.identityservice.controller;

import com.autoservice.identityservice.common.ApiResponse;
import com.autoservice.identityservice.dto.response.MechanicResponse;
import com.autoservice.identityservice.service.MechanicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mechanics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MECHANIC')")
public class MechanicProfileController {

    private final MechanicService mechanicService;

    @GetMapping("/me")
    public ApiResponse<MechanicResponse> getMyProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long mechanicUserId = Long.valueOf(jwt.getSubject());

        MechanicResponse profile =
                mechanicService.getMechanicByUserId(mechanicUserId);

        return ApiResponse.success(
                "Lấy hồ sơ kỹ thuật viên thành công.",
                profile
        );
    }
}