package com.autoservice.inventoryservice.service;

import com.autoservice.inventoryservice.domain.entity.InventoryTransaction;
import com.autoservice.inventoryservice.domain.entity.Part;
import com.autoservice.inventoryservice.domain.enums.InventoryTransactionType;
import com.autoservice.inventoryservice.dto.request.AdjustStockRequest;
import com.autoservice.inventoryservice.dto.request.CreatePartRequest;
import com.autoservice.inventoryservice.dto.request.StockInRequest;
import com.autoservice.inventoryservice.dto.request.StockOutRequest;
import com.autoservice.inventoryservice.dto.response.PartResponse;
import com.autoservice.inventoryservice.exception.DuplicateResourceException;
import com.autoservice.inventoryservice.exception.InsufficientStockException;
import com.autoservice.inventoryservice.exception.ResourceNotFoundException;
import com.autoservice.inventoryservice.mapper.InventoryMapper;
import com.autoservice.inventoryservice.repository.InventoryTransactionRepository;
import com.autoservice.inventoryservice.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;

    private final InventoryTransactionRepository
            inventoryTransactionRepository;

    private final InventoryMapper inventoryMapper;

    @Transactional
    public PartResponse createPart(
            Long performedByUserId,
            CreatePartRequest request
    ) {
        String normalizedPartCode =
                request.partCode()
                        .trim()
                        .toUpperCase();

        if (partRepository.existsByPartCodeIgnoreCase(
                normalizedPartCode
        )) {
            throw new DuplicateResourceException(
                    "Mã phụ tùng đã tồn tại."
            );
        }

        Part part = Part.builder()
                .partCode(normalizedPartCode)
                .name(request.name().trim())
                .description(request.description())
                .manufacturer(request.manufacturer())
                .unit(request.unit().trim())
                .unitPrice(request.unitPrice())
                .quantityInStock(request.initialQuantity())
                .minimumStock(request.minimumStock())
                .active(true)
                .build();

        Part savedPart =
                partRepository.save(part);

        if (request.initialQuantity() > 0) {
            saveTransaction(
                    savedPart,
                    InventoryTransactionType.STOCK_IN,
                    request.initialQuantity(),
                    0,
                    request.initialQuantity(),
                    "INITIAL_STOCK",
                    null,
                    "Số lượng tồn kho ban đầu.",
                    performedByUserId
            );
        }

        return inventoryMapper.toPartResponse(savedPart);
    }

    @Transactional(readOnly = true)
    public PartResponse getPart(
            Long partId
    ) {
        return inventoryMapper.toPartResponse(
                findPartById(partId)
        );
    }

    @Transactional(readOnly = true)
    public Page<PartResponse> getParts(
            String keyword,
            Boolean active,
            Boolean lowStock,
            Pageable pageable
    ) {
        String normalizedKeyword =
                StringUtils.hasText(keyword)
                        ? keyword.trim()
                        : null;

        return partRepository
                .searchParts(
                        normalizedKeyword,
                        active,
                        lowStock,
                        pageable
                )
                .map(
                        inventoryMapper::toPartResponse
                );
    }

    @Transactional
    public PartResponse stockIn(
            Long partId,
            Long performedByUserId,
            StockInRequest request
    ) {
        Part part = findPartById(partId);

        int quantityBefore =
                part.getQuantityInStock();

        int quantityAfter =
                quantityBefore + request.quantity();

        part.setQuantityInStock(quantityAfter);

        Part savedPart =
                partRepository.save(part);

        saveTransaction(
                savedPart,
                InventoryTransactionType.STOCK_IN,
                request.quantity(),
                quantityBefore,
                quantityAfter,
                request.referenceType(),
                request.referenceId(),
                request.note(),
                performedByUserId
        );

        return inventoryMapper.toPartResponse(savedPart);
    }

    @Transactional
    public PartResponse stockOut(
            Long partId,
            Long performedByUserId,
            StockOutRequest request
    ) {
        Part part = findPartById(partId);

        int quantityBefore =
                part.getQuantityInStock();

        if (request.quantity() > quantityBefore) {
            throw new InsufficientStockException(
                    "Số lượng phụ tùng trong kho không đủ. "
                            + "Tồn kho hiện tại: "
                            + quantityBefore
                            + "."
            );
        }

        int quantityAfter =
                quantityBefore - request.quantity();

        part.setQuantityInStock(quantityAfter);

        Part savedPart =
                partRepository.save(part);

        saveTransaction(
                savedPart,
                InventoryTransactionType.STOCK_OUT,
                request.quantity(),
                quantityBefore,
                quantityAfter,
                request.referenceType(),
                request.referenceId(),
                request.note(),
                performedByUserId
        );

        return inventoryMapper.toPartResponse(savedPart);
    }

    @Transactional
    public PartResponse adjustStock(
            Long partId,
            Long performedByUserId,
            AdjustStockRequest request
    ) {
        Part part = findPartById(partId);

        int quantityBefore =
                part.getQuantityInStock();

        int quantityAfter =
                request.newQuantity();

        if (quantityBefore == quantityAfter) {
            return inventoryMapper.toPartResponse(part);
        }

        int adjustedQuantity =
                Math.abs(
                        quantityAfter - quantityBefore
                );

        part.setQuantityInStock(quantityAfter);

        Part savedPart =
                partRepository.save(part);

        saveTransaction(
                savedPart,
                InventoryTransactionType.ADJUSTMENT,
                adjustedQuantity,
                quantityBefore,
                quantityAfter,
                "STOCKTAKE",
                null,
                request.note(),
                performedByUserId
        );

        return inventoryMapper.toPartResponse(savedPart);
    }

    @Transactional(readOnly = true)
    public Part findPartById(
            Long partId
    ) {
        return partRepository
                .findById(partId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy phụ tùng."
                        )
                );
    }

    private void saveTransaction(
            Part part,
            InventoryTransactionType transactionType,
            Integer quantity,
            Integer quantityBefore,
            Integer quantityAfter,
            String referenceType,
            Long referenceId,
            String note,
            Long performedByUserId
    ) {
        InventoryTransaction transaction =
                InventoryTransaction.builder()
                        .part(part)
                        .transactionType(transactionType)
                        .quantity(quantity)
                        .quantityBefore(quantityBefore)
                        .quantityAfter(quantityAfter)
                        .referenceType(referenceType)
                        .referenceId(referenceId)
                        .note(note)
                        .performedByUserId(
                                performedByUserId
                        )
                        .build();

        inventoryTransactionRepository.save(
                transaction
        );
    }
}