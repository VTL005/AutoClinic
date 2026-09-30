package com.autoservice.billingservice.repository;

import com.autoservice.billingservice.domain.entity.Invoice;
import com.autoservice.billingservice.domain.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository
        extends JpaRepository<Invoice, Long>,
        JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByInvoiceCode(
            String invoiceCode
    );

    Optional<Invoice> findByRepairOrderId(
            Long repairOrderId
    );

    Optional<Invoice> findByIdAndCustomerUserId(
            Long invoiceId,
            Long customerUserId
    );

    boolean existsByInvoiceCode(
            String invoiceCode
    );

    boolean existsByRepairOrderId(
            Long repairOrderId
    );

    Page<Invoice> findByCustomerUserId(
            Long customerUserId,
            Pageable pageable
    );

    Page<Invoice> findByStatus(
            InvoiceStatus status,
            Pageable pageable
    );
}