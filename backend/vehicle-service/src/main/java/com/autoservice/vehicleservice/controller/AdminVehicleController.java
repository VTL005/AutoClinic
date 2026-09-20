package com.autoservice.vehicleservice.controller;

import com.autoservice.vehicleservice.common.ApiResponse;
import com.autoservice.vehicleservice.domain.enums.VehicleStatus;
import com.autoservice.vehicleservice.dto.response.PageResponse;
import com.autoservice.vehicleservice.dto.response.VehicleResponse;
import com.autoservice.vehicleservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/admin/vehicles")
@RequiredArgsConstructor
public class AdminVehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<VehicleResponse>>
            > getVehicles(
            @RequestParam(required = false)
            Long ownerUserId,
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
        PageResponse<VehicleResponse> vehicles =
                vehicleService.getVehiclesForAdmin(
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
    getVehicle(
            @PathVariable Long vehicleId
    ) {
        VehicleResponse vehicle =
                vehicleService.getVehicleForAdmin(
                        vehicleId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin phương tiện thành công.",
                        vehicle
                )
        );
    }
}