package com.autoservice.inventoryservice.repository;

import com.autoservice.inventoryservice.domain.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long> {

    Page<InventoryTransaction>
    findByPart_Id(
            Long partId,
            Pageable pageable
    );
}