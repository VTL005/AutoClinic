package com.autoservice.inventoryservice.mapper;

import com.autoservice.inventoryservice.domain.entity.InventoryTransaction;
import com.autoservice.inventoryservice.domain.entity.Part;
import com.autoservice.inventoryservice.dto.response.InventoryTransactionResponse;
import com.autoservice.inventoryservice.dto.response.PartResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public PartResponse toPartResponse(
            Part part
    ) {
        boolean lowStock =
                part.getQuantityInStock()
                        <= part.getMinimumStock();

        return new PartResponse(
                part.getId(),
                part.getPartCode(),
                part.getName(),
                part.getDescription(),
                part.getManufacturer(),
                part.getUnit(),
                part.getUnitPrice(),
                part.getQuantityInStock(),
                part.getMinimumStock(),
                lowStock,
                part.getActive(),
                part.getCreatedAt(),
                part.getUpdatedAt()
        );
    }

    public InventoryTransactionResponse
    toTransactionResponse(
            InventoryTransaction transaction
    ) {
        return new InventoryTransactionResponse(
                transaction.getId(),
                transaction.getPart().getId(),
                transaction.getPart().getPartCode(),
                transaction.getTransactionType(),
                transaction.getQuantity(),
                transaction.getQuantityBefore(),
                transaction.getQuantityAfter(),
                transaction.getReferenceType(),
                transaction.getReferenceId(),
                transaction.getNote(),
                transaction.getPerformedByUserId(),
                transaction.getCreatedAt()
        );
    }
}