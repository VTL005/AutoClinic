package com.autoservice.inventoryservice.controller;

import com.autoservice.inventoryservice.common.ApiResponse;
import com.autoservice.inventoryservice.dto.response.InventoryTransactionResponse;
import com.autoservice.inventoryservice.service.InventoryTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryTransactionController {

    private final InventoryTransactionService
            inventoryTransactionService;

    @GetMapping
    public ApiResponse<
            Page<InventoryTransactionResponse>
            >
    getTransactions(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<InventoryTransactionResponse> response =
                inventoryTransactionService
                        .getTransactions(pageable);

        return ApiResponse.success(
                "Lấy lịch sử giao dịch kho thành công.",
                response
        );
    }

    @GetMapping("/parts/{partId}")
    public ApiResponse<
            Page<InventoryTransactionResponse>
            >
    getTransactionsByPart(
            @PathVariable Long partId,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<InventoryTransactionResponse> response =
                inventoryTransactionService
                        .getTransactionsByPart(
                                partId,
                                pageable
                        );

        return ApiResponse.success(
                "Lấy lịch sử phụ tùng thành công.",
                response
        );
    }
}