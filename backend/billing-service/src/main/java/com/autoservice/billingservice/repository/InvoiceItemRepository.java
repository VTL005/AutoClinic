package com.autoservice.billingservice.repository;

import com.autoservice.billingservice.domain.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository
        extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findByInvoice_IdOrderByIdAsc(
            Long invoiceId
    );

    void deleteByInvoice_Id(
            Long invoiceId
    );
}