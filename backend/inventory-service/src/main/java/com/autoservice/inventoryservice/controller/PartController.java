package com.autoservice.inventoryservice.controller;

import com.autoservice.inventoryservice.common.ApiResponse;
import com.autoservice.inventoryservice.dto.request.AdjustStockRequest;
import com.autoservice.inventoryservice.dto.request.CreatePartRequest;
import com.autoservice.inventoryservice.dto.request.StockInRequest;
import com.autoservice.inventoryservice.dto.request.StockOutRequest;
import com.autoservice.inventoryservice.dto.response.PartResponse;
import com.autoservice.inventoryservice.service.PartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PartController {

    private final PartService partService;

    @PostMapping
    public ResponseEntity<ApiResponse<PartResponse>>
    createPart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            CreatePartRequest request
    ) {
        PartResponse response =
                partService.createPart(
                        getUserId(jwt),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo phụ tùng thành công.",
                                response
                        )
                );
    }

    @GetMapping
    public ApiResponse<Page<PartResponse>>
    getParts(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Boolean active,

            @RequestParam(required = false)
            Boolean lowStock,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<PartResponse> response =
                partService.getParts(
                        keyword,
                        active,
                        lowStock,
                        pageable
                );

        return ApiResponse.success(
                "Lấy danh sách phụ tùng thành công.",
                response
        );
    }

    @GetMapping("/{partId}")
    public ApiResponse<PartResponse>
    getPart(
            @PathVariable Long partId
    ) {
        PartResponse response =
                partService.getPart(partId);

        return ApiResponse.success(
                "Lấy thông tin phụ tùng thành công.",
                response
        );
    }

    @PatchMapping("/{partId}/stock-in")
    public ApiResponse<PartResponse>
    stockIn(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long partId,
            @Valid @RequestBody
            StockInRequest request
    ) {
        PartResponse response =
                partService.stockIn(
                        partId,
                        getUserId(jwt),
                        request
                );

        return ApiResponse.success(
                "Nhập kho thành công.",
                response
        );
    }

    @PatchMapping("/{partId}/stock-out")
    public ApiResponse<PartResponse>
    stockOut(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long partId,
            @Valid @RequestBody
            StockOutRequest request
    ) {
        PartResponse response =
                partService.stockOut(
                        partId,
                        getUserId(jwt),
                        request
                );

        return ApiResponse.success(
                "Xuất kho thành công.",
                response
        );
    }

    @PatchMapping("/{partId}/adjust-stock")
    public ApiResponse<PartResponse>
    adjustStock(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long partId,
            @Valid @RequestBody
            AdjustStockRequest request
    ) {
        PartResponse response =
                partService.adjustStock(
                        partId,
                        getUserId(jwt),
                        request
                );

        return ApiResponse.success(
                "Điều chỉnh tồn kho thành công.",
                response
        );
    }

    private Long getUserId(
            Jwt jwt
    ) {
        return Long.valueOf(
                jwt.getSubject()
        );
    }
}