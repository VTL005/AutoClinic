package com.autoservice.billingservice.service;

import com.autoservice.billingservice.client.RepairClient;
import com.autoservice.billingservice.client.dto.RepairOrderDetailsResponse;
import com.autoservice.billingservice.client.dto.RepairTaskDetailsResponse;
import com.autoservice.billingservice.domain.entity.Invoice;
import com.autoservice.billingservice.domain.entity.InvoiceItem;
import com.autoservice.billingservice.domain.enums.InvoiceItemType;
import com.autoservice.billingservice.domain.enums.InvoiceStatus;
import com.autoservice.billingservice.dto.request.AddInvoiceItemRequest;
import com.autoservice.billingservice.dto.request.CreateInvoiceRequest;
import com.autoservice.billingservice.dto.response.InvoiceResponse;
import com.autoservice.billingservice.exception.DuplicateResourceException;
import com.autoservice.billingservice.exception.InvalidInvoiceStateException;
import com.autoservice.billingservice.exception.ResourceNotFoundException;
import com.autoservice.billingservice.mapper.BillingMapper;
import com.autoservice.billingservice.repository.InvoiceItemRepository;
import com.autoservice.billingservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    private final InvoiceItemRepository
            invoiceItemRepository;

    private final RepairClient repairClient;

    private final BillingMapper billingMapper;

    @Transactional
    public InvoiceResponse createInvoice(
            Long createdByUserId,
            String authorizationHeader,
            CreateInvoiceRequest request
    ) {
        if (invoiceRepository.existsByRepairOrderId(
                request.repairOrderId()
        )) {
            throw new DuplicateResourceException(
                    "Phiếu sửa chữa đã có hóa đơn."
            );
        }

        RepairOrderDetailsResponse repairOrder =
                repairClient.getRepairOrder(
                        request.repairOrderId(),
                        authorizationHeader
                );

        validateRepairOrderStatus(repairOrder);

        Invoice invoice = Invoice.builder()
                .invoiceCode(generateInvoiceCode())
                .repairOrderId(repairOrder.id())
                .customerUserId(
                        repairOrder.customerUserId()
                )
                .vehicleId(repairOrder.vehicleId())
                .createdByUserId(createdByUserId)
                .subtotal(BigDecimal.ZERO)
                .discountAmount(
                        valueOrZero(
                                request.discountAmount()
                        )
                )
                .taxAmount(
                        valueOrZero(
                                request.taxAmount()
                        )
                )
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.DRAFT)
                .dueAt(request.dueAt())
                .note(request.note())
                .build();

        addLaborItemsFromRepairOrder(
                invoice,
                repairOrder
        );

        recalculateInvoice(invoice);

        Invoice savedInvoice =
                invoiceRepository.save(invoice);

        return billingMapper.toInvoiceResponse(
                savedInvoice
        );
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(
            Long invoiceId
    ) {
        Invoice invoice =
                findInvoiceById(invoiceId);

        return billingMapper.toInvoiceResponse(
                invoice
        );
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getInvoices(
            InvoiceStatus status,
            Pageable pageable
    ) {
        Page<Invoice> invoices =
                status == null
                        ? invoiceRepository
                        .findAll(pageable)
                        : invoiceRepository
                        .findByStatus(
                                status,
                                pageable
                        );

        return invoices.map(
                billingMapper::toInvoiceResponse
        );
    }

    @Transactional
    public InvoiceResponse addInvoiceItem(
            Long invoiceId,
            AddInvoiceItemRequest request
    ) {
        Invoice invoice =
                findInvoiceById(invoiceId);

        validateDraftInvoice(invoice);

        BigDecimal lineTotal =
                request.quantity()
                        .multiply(
                                request.unitPrice()
                        );

        InvoiceItem item =
                InvoiceItem.builder()
                        .invoice(invoice)
                        .itemType(
                                request.itemType()
                        )
                        .referenceId(
                                request.referenceId()
                        )
                        .itemName(
                                request.itemName().trim()
                        )
                        .description(
                                request.description()
                        )
                        .quantity(
                                request.quantity()
                        )
                        .unitPrice(
                                request.unitPrice()
                        )
                        .lineTotal(lineTotal)
                        .build();

        invoice.getItems().add(item);

        invoiceItemRepository.save(item);

        recalculateInvoice(invoice);

        Invoice savedInvoice =
                invoiceRepository.save(invoice);

        return billingMapper.toInvoiceResponse(
                savedInvoice
        );
    }

    @Transactional
    public InvoiceResponse issueInvoice(
            Long invoiceId
    ) {
        Invoice invoice =
                findInvoiceById(invoiceId);

        validateDraftInvoice(invoice);

        if (invoice.getItems().isEmpty()) {
            throw new InvalidInvoiceStateException(
                    "Không thể phát hành hóa đơn "
                            + "không có dòng chi phí."
            );
        }

        recalculateInvoice(invoice);

        if (invoice.getTotalAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInvoiceStateException(
                    "Tổng tiền hóa đơn phải lớn hơn 0."
            );
        }

        invoice.setStatus(
                InvoiceStatus.ISSUED
        );

        invoice.setIssuedAt(
                LocalDateTime.now()
        );

        Invoice savedInvoice =
                invoiceRepository.save(invoice);

        return billingMapper.toInvoiceResponse(
                savedInvoice
        );
    }

    @Transactional(readOnly = true)
    public Invoice findInvoiceById(
            Long invoiceId
    ) {
        return invoiceRepository
                .findById(invoiceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy hóa đơn."
                        )
                );
    }

    private void addLaborItemsFromRepairOrder(
            Invoice invoice,
            RepairOrderDetailsResponse repairOrder
    ) {
        List<RepairTaskDetailsResponse> tasks =
                repairOrder.tasks() == null
                        ? List.of()
                        : repairOrder.tasks();

        for (RepairTaskDetailsResponse task : tasks) {
            BigDecimal laborCost =
                    valueOrZero(task.laborCost());

            if (laborCost.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {
                continue;
            }

            InvoiceItem item =
                    InvoiceItem.builder()
                            .invoice(invoice)
                            .itemType(
                                    InvoiceItemType.LABOR
                            )
                            .referenceId(task.id())
                            .itemName(task.taskName())
                            .description(
                                    task.description()
                            )
                            .quantity(BigDecimal.ONE)
                            .unitPrice(laborCost)
                            .lineTotal(laborCost)
                            .build();

            invoice.getItems().add(item);
        }
    }

    private void recalculateInvoice(
            Invoice invoice
    ) {
        BigDecimal subtotal =
                invoice.getItems()
                        .stream()
                        .map(
                                InvoiceItem::getLineTotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal discountAmount =
                valueOrZero(
                        invoice.getDiscountAmount()
                );

        BigDecimal taxAmount =
                valueOrZero(
                        invoice.getTaxAmount()
                );

        BigDecimal totalAmount =
                subtotal
                        .subtract(discountAmount)
                        .add(taxAmount);

        if (totalAmount.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            throw new InvalidInvoiceStateException(
                    "Số tiền giảm giá không được "
                            + "lớn hơn tổng chi phí và thuế."
            );
        }

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(
                discountAmount
        );
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(totalAmount);
    }

    private void validateRepairOrderStatus(
            RepairOrderDetailsResponse repairOrder
    ) {
        String status = repairOrder.status();

        if (!"COMPLETED".equals(status)
                && !"DELIVERED".equals(status)) {
            throw new InvalidInvoiceStateException(
                    "Chỉ được lập hóa đơn cho phiếu "
                            + "sửa chữa đã hoàn thành "
                            + "hoặc đã bàn giao."
            );
        }
    }

    private void validateDraftInvoice(
            Invoice invoice
    ) {
        if (invoice.getStatus()
                != InvoiceStatus.DRAFT) {
            throw new InvalidInvoiceStateException(
                    "Chỉ được thay đổi hóa đơn "
                            + "đang ở trạng thái DRAFT."
            );
        }
    }

    private String generateInvoiceCode() {
        return "INV-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    private BigDecimal valueOrZero(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}