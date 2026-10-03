package com.autoservice.identityservice.controller;

import com.autoservice.identityservice.common.ApiResponse;
import com.autoservice.identityservice.domain.enums.EmploymentStatus;
import com.autoservice.identityservice.domain.enums.SkillLevel;
import com.autoservice.identityservice.dto.request.CreateMechanicRequest;
import com.autoservice.identityservice.dto.request.UpdateMechanicProfileRequest;
import com.autoservice.identityservice.dto.response.MechanicResponse;
import com.autoservice.identityservice.dto.response.PageResponse;
import com.autoservice.identityservice.service.MechanicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/mechanics")
@RequiredArgsConstructor
public class AdminMechanicController {

    private final MechanicService mechanicService;

    @PostMapping
    public ResponseEntity<ApiResponse<MechanicResponse>>
    createMechanic(
            @Valid
            @RequestBody
            CreateMechanicRequest request
    ) {
        MechanicResponse mechanic =
                mechanicService.createMechanic(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo kỹ thuật viên thành công.",
                                mechanic
                        )
                );
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<MechanicResponse>>
            > getMechanics(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            SkillLevel skillLevel,

            @RequestParam(required = false)
            EmploymentStatus employmentStatus,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        PageResponse<MechanicResponse> mechanics =
                mechanicService.getMechanics(
                        keyword,
                        skillLevel,
                        employmentStatus,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách kỹ thuật viên thành công.",
                        mechanics
                )
        );
    }
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ApiResponse<MechanicResponse>>
    getMechanicByUserId(
            @PathVariable("userId")
            Long userId
    ) {
        MechanicResponse mechanic =
                mechanicService.getMechanicByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin kỹ thuật viên thành công.",
                        mechanic
                )
        );
    }
    @GetMapping("/{profileId}")
    public ResponseEntity<ApiResponse<MechanicResponse>>
    getMechanic(
            @PathVariable
            Long profileId
    ) {
        MechanicResponse mechanic =
                mechanicService.getMechanic(profileId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin kỹ thuật viên thành công.",
                        mechanic
                )
        );
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<ApiResponse<MechanicResponse>>
    updateMechanic(
            @PathVariable
            Long profileId,

            @Valid
            @RequestBody
            UpdateMechanicProfileRequest request
    ) {
        MechanicResponse mechanic =
                mechanicService.updateMechanic(
                        profileId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật kỹ thuật viên thành công.",
                        mechanic
                )
        );
    }
}