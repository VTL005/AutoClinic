package com.autoservice.inventoryservice.service;

import com.autoservice.inventoryservice.domain.entity.Part;
import com.autoservice.inventoryservice.dto.request.UpdatePartRequest;
import com.autoservice.inventoryservice.dto.request.UpdatePartStatusRequest;
import com.autoservice.inventoryservice.dto.response.PartResponse;
import com.autoservice.inventoryservice.mapper.InventoryMapper;
import com.autoservice.inventoryservice.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartUpdateService {

    private final PartRepository partRepository;

    private final PartService partService;

    private final InventoryMapper inventoryMapper;

    @Transactional
    public PartResponse updatePart(
            Long partId,
            UpdatePartRequest request
    ) {
        Part part =
                partService.findPartById(partId);

        part.setName(request.name().trim());
        part.setDescription(request.description());
        part.setManufacturer(request.manufacturer());
        part.setUnit(request.unit().trim());
        part.setUnitPrice(request.unitPrice());
        part.setMinimumStock(
                request.minimumStock()
        );

        Part savedPart =
                partRepository.save(part);

        return inventoryMapper.toPartResponse(
                savedPart
        );
    }

    @Transactional
    public PartResponse updateStatus(
            Long partId,
            UpdatePartStatusRequest request
    ) {
        Part part =
                partService.findPartById(partId);

        part.setActive(request.active());

        Part savedPart =
                partRepository.save(part);

        return inventoryMapper.toPartResponse(
                savedPart
        );
    }
}