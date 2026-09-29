package com.autoservice.inventoryservice.controller;

import com.autoservice.inventoryservice.common.ApiResponse;
import com.autoservice.inventoryservice.dto.request.UpdatePartRequest;
import com.autoservice.inventoryservice.dto.request.UpdatePartStatusRequest;
import com.autoservice.inventoryservice.dto.response.PartResponse;
import com.autoservice.inventoryservice.service.PartUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PartUpdateController {

    private final PartUpdateService
            partUpdateService;

    @PutMapping("/{partId}")
    public ApiResponse<PartResponse>
    updatePart(
            @PathVariable Long partId,
            @Valid @RequestBody
            UpdatePartRequest request
    ) {
        PartResponse response =
                partUpdateService.updatePart(
                        partId,
                        request
                );

        return ApiResponse.success(
                "Cập nhật phụ tùng thành công.",
                response
        );
    }

    @PatchMapping("/{partId}/status")
    public ApiResponse<PartResponse>
    updateStatus(
            @PathVariable Long partId,
            @Valid @RequestBody
            UpdatePartStatusRequest request
    ) {
        PartResponse response =
                partUpdateService.updateStatus(
                        partId,
                        request
                );

        return ApiResponse.success(
                "Cập nhật trạng thái phụ tùng thành công.",
                response
        );
    }
}