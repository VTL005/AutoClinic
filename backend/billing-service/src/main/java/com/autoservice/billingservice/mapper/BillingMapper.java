package com.autoservice.billingservice.mapper;

import com.autoservice.billingservice.domain.entity.Invoice;
import com.autoservice.billingservice.domain.entity.InvoiceItem;
import com.autoservice.billingservice.domain.entity.Payment;
import com.autoservice.billingservice.dto.response.InvoiceItemResponse;
import com.autoservice.billingservice.dto.response.InvoiceResponse;
import com.autoservice.billingservice.dto.response.PaymentResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BillingMapper {

    public InvoiceItemResponse toInvoiceItemResponse(
            InvoiceItem item
    ) {
        return new InvoiceItemResponse(
                item.getId(),
                item.getItemType(),
                item.getReferenceId(),
                item.getItemName(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal(),
                item.getCreatedAt()
        );
    }

    public PaymentResponse toPaymentResponse(
            Payment payment
    ) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentCode(),
                payment.getInvoice().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getProviderOrderCode(),
                payment.getCheckoutUrl(),
                payment.getQrCode(),
                payment.getExpiresAt(),
                payment.getStatus(),
                payment.getTransactionReference(),
                payment.getPaidAt(),
                payment.getWebhookReceivedAt(),
                payment.getFailureReason(),
                payment.getReceivedByUserId(),
                payment.getNote(),
                payment.getCreatedAt()
        );
    }

    public InvoiceResponse toInvoiceResponse(
            Invoice invoice
    ) {
        List<InvoiceItemResponse> itemResponses =
                invoice.getItems()
                        .stream()
                        .map(this::toInvoiceItemResponse)
                        .toList();

        List<PaymentResponse> paymentResponses =
                invoice.getPayments()
                        .stream()
                        .map(this::toPaymentResponse)
                        .toList();

        BigDecimal totalAmount =
                valueOrZero(
                        invoice.getTotalAmount()
                );

        BigDecimal paidAmount =
                valueOrZero(
                        invoice.getPaidAmount()
                );

        BigDecimal balanceDue =
                totalAmount.subtract(paidAmount);

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceCode(),
                invoice.getRepairOrderId(),
                invoice.getCustomerUserId(),
                invoice.getVehicleId(),
                invoice.getCreatedByUserId(),
                valueOrZero(invoice.getSubtotal()),
                valueOrZero(
                        invoice.getDiscountAmount()
                ),
                valueOrZero(invoice.getTaxAmount()),
                totalAmount,
                paidAmount,
                balanceDue,
                invoice.getStatus(),
                invoice.getIssuedAt(),
                invoice.getDueAt(),
                invoice.getPaidAt(),
                invoice.getNote(),
                itemResponses,
                paymentResponses,
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

    private BigDecimal valueOrZero(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}