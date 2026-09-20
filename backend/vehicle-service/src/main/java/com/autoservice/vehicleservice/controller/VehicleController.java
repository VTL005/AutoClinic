package com.autoservice.vehicleservice.controller;

import com.autoservice.vehicleservice.common.ApiResponse;
import com.autoservice.vehicleservice.domain.enums.VehicleStatus;
import com.autoservice.vehicleservice.dto.request.CreateVehicleRequest;
import com.autoservice.vehicleservice.dto.request.UpdateVehicleRequest;
import com.autoservice.vehicleservice.dto.response.PageResponse;
import com.autoservice.vehicleservice.dto.response.VehicleResponse;
import com.autoservice.vehicleservice.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>>
    createVehicle(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateVehicleRequest request
    ) {
        Long ownerUserId = extractUserId(jwt);

        VehicleResponse vehicle =
                vehicleService.createVehicle(
                        ownerUserId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Thêm phương tiện thành công.",
                                vehicle
                        )
                );
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<VehicleResponse>>
            > getMyVehicles(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            String keyword,
            @RequestParam(required = false)
            VehicleStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = DESC
            )
            Pageable pageable
    ) {
        Long ownerUserId = extractUserId(jwt);

        PageResponse<VehicleResponse> vehicles =
                vehicleService.getMyVehicles(
                        ownerUserId,
                        keyword,
                        status,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách phương tiện thành công.",
                        vehicles
                )
        );
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>>
    getMyVehicle(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long vehicleId
    ) {
        Long ownerUserId = extractUserId(jwt);

        VehicleResponse vehicle =
                vehicleService.getMyVehicle(
                        ownerUserId,
                        vehicleId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin phương tiện thành công.",
                        vehicle
                )
        );
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>>
    updateMyVehicle(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long vehicleId,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {
        Long ownerUserId = extractUserId(jwt);

        VehicleResponse vehicle =
                vehicleService.updateMyVehicle(
                        ownerUserId,
                        vehicleId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật phương tiện thành công.",
                        vehicle
                )
        );
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<Void>>
    deleteMyVehicle(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long vehicleId
    ) {
        Long ownerUserId = extractUserId(jwt);

        vehicleService.deleteMyVehicle(
                ownerUserId,
                vehicleId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xóa phương tiện thành công.",
                        null
                )
        );
    }

    private Long extractUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}