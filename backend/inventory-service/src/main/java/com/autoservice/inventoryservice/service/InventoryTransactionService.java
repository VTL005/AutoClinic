package com.autoservice.inventoryservice.service;

import com.autoservice.inventoryservice.dto.response.InventoryTransactionResponse;
import com.autoservice.inventoryservice.exception.ResourceNotFoundException;
import com.autoservice.inventoryservice.mapper.InventoryMapper;
import com.autoservice.inventoryservice.repository.InventoryTransactionRepository;
import com.autoservice.inventoryservice.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryTransactionService {

    private final InventoryTransactionRepository
            inventoryTransactionRepository;

    private final PartRepository partRepository;

    private final InventoryMapper inventoryMapper;

    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse>
    getTransactions(
            Pageable pageable
    ) {
        return inventoryTransactionRepository
                .findAll(pageable)
                .map(
                        inventoryMapper
                                ::toTransactionResponse
                );
    }

    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse>
    getTransactionsByPart(
            Long partId,
            Pageable pageable
    ) {
        if (!partRepository.existsById(partId)) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy phụ tùng."
            );
        }

        return inventoryTransactionRepository
                .findByPart_Id(
                        partId,
                        pageable
                )
                .map(
                        inventoryMapper
                                ::toTransactionResponse
                );
    }
}