package com.autoservice.billingservice.service;

import com.autoservice.billingservice.domain.entity.Invoice;
import com.autoservice.billingservice.dto.response.InvoiceResponse;
import com.autoservice.billingservice.exception.ResourceNotFoundException;
import com.autoservice.billingservice.mapper.BillingMapper;
import com.autoservice.billingservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerInvoiceService {

    private final InvoiceRepository invoiceRepository;

    private final BillingMapper billingMapper;

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getCustomerInvoices(
            Long customerUserId,
            Pageable pageable
    ) {
        return invoiceRepository
                .findByCustomerUserId(
                        customerUserId,
                        pageable
                )
                .map(
                        billingMapper::toInvoiceResponse
                );
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getCustomerInvoice(
            Long invoiceId,
            Long customerUserId
    ) {
        Invoice invoice =
                findCustomerInvoice(
                        invoiceId,
                        customerUserId
                );

        return billingMapper.toInvoiceResponse(
                invoice
        );
    }

    @Transactional(readOnly = true)
    public Invoice findCustomerInvoice(
            Long invoiceId,
            Long customerUserId
    ) {
        return invoiceRepository
                .findByIdAndCustomerUserId(
                        invoiceId,
                        customerUserId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy hóa đơn."
                        )
                );
    }
}