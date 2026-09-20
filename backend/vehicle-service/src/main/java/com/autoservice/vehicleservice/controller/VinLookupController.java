package com.autoservice.vehicleservice.controller;

import com.autoservice.vehicleservice.common.ApiResponse;
import com.autoservice.vehicleservice.dto.response.VinLookupResponse;
import com.autoservice.vehicleservice.service.VinLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vin")
@RequiredArgsConstructor
public class VinLookupController {

    private final VinLookupService vinLookupService;

    @GetMapping("/{vin}")
    public ResponseEntity<ApiResponse<VinLookupResponse>>
    lookupVin(
            @PathVariable String vin
    ) {
        VinLookupResponse result =
                vinLookupService.lookup(vin);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tra cứu thông tin VIN thành công.",
                        result
                )
        );
    }
}