package com.autoservice.billingservice.service;

import com.autoservice.billingservice.domain.entity.Invoice;
import com.autoservice.billingservice.domain.entity.Payment;
import com.autoservice.billingservice.domain.enums.InvoiceStatus;
import com.autoservice.billingservice.domain.enums.PaymentMethod;
import com.autoservice.billingservice.domain.enums.PaymentStatus;
import com.autoservice.billingservice.dto.request.ConfirmCashPaymentRequest;
import com.autoservice.billingservice.dto.request.InitiateOnlinePaymentRequest;
import com.autoservice.billingservice.dto.response.InvoiceResponse;
import com.autoservice.billingservice.dto.response.PaymentResponse;
import com.autoservice.billingservice.exception.InvalidInvoiceStateException;
import com.autoservice.billingservice.exception.ResourceNotFoundException;
import com.autoservice.billingservice.mapper.BillingMapper;
import com.autoservice.billingservice.repository.InvoiceRepository;
import com.autoservice.billingservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final InvoiceRepository invoiceRepository;

    private final InvoiceService invoiceService;

    private final BillingMapper billingMapper;

    @Transactional
    public InvoiceResponse confirmCashPayment(
            Long invoiceId,
            Long receivedByUserId,
            ConfirmCashPaymentRequest request
    ) {
        Invoice invoice =
                invoiceService.findInvoiceById(
                        invoiceId
                );

        validateInvoiceCanBePaid(invoice);

        validatePaymentAmount(
                invoice,
                request.amount()
        );

        LocalDateTime now =
                LocalDateTime.now();

        Payment payment = Payment.builder()
                .paymentCode(generatePaymentCode())
                .invoice(invoice)
                .amount(request.amount())
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED)
                .paidAt(now)
                .receivedByUserId(
                        receivedByUserId
                )
                .note(request.note())
                .build();

        Payment savedPayment =
                paymentRepository.save(payment);

        invoice.getPayments().add(
                savedPayment
        );

        applyCompletedPayment(
                invoice,
                request.amount(),
                now
        );

        Invoice savedInvoice =
                invoiceRepository.save(invoice);

        return billingMapper.toInvoiceResponse(
                savedInvoice
        );
    }

    @Transactional
    public PaymentResponse createOnlinePaymentAttempt(
            Long invoiceId,
            Long customerUserId,
            InitiateOnlinePaymentRequest request
    ) {
        Invoice invoice =
                invoiceService.findInvoiceById(
                        invoiceId
                );

        validateCustomerOwnsInvoice(
                invoice,
                customerUserId
        );

        validateInvoiceCanBePaid(invoice);

        validateOnlinePaymentMethod(
                request.paymentMethod()
        );

        validatePaymentAmount(
                invoice,
                request.amount()
        );

        expireOrRejectExistingPendingPayment(
                invoiceId
        );

        LocalDateTime now =
                LocalDateTime.now();

        String providerOrderCode =
                generateProviderOrderCode(
                        request.paymentMethod()
                );

        Payment payment = Payment.builder()
                .paymentCode(generatePaymentCode())
                .invoice(invoice)
                .amount(request.amount())
                .paymentMethod(
                        request.paymentMethod()
                )
                .providerOrderCode(
                        providerOrderCode
                )
                .expiresAt(
                        now.plusMinutes(15)
                )
                .status(PaymentStatus.PENDING)
                .receivedByUserId(
                        customerUserId
                )
                .note(
                        "Khởi tạo giao dịch thanh toán online."
                )
                .build();

        Payment savedPayment =
                paymentRepository.save(payment);

        return billingMapper.toPaymentResponse(
                savedPayment
        );
    }

    @Transactional
    public PaymentResponse updateGatewayDetails(
            Long paymentId,
            String providerOrderCode,
            String checkoutUrl,
            String qrCode,
            LocalDateTime expiresAt
    ) {
        Payment payment =
                findPaymentById(paymentId);

        if (payment.getStatus()
                != PaymentStatus.PENDING) {
            throw new InvalidInvoiceStateException(
                    "Chỉ được cập nhật thông tin cho "
                            + "giao dịch đang chờ thanh toán."
            );
        }

        if (providerOrderCode != null
                && !providerOrderCode.isBlank()) {
            payment.setProviderOrderCode(
                    providerOrderCode
            );
        }

        payment.setCheckoutUrl(checkoutUrl);
        payment.setQrCode(qrCode);

        if (expiresAt != null) {
            payment.setExpiresAt(expiresAt);
        }

        Payment savedPayment =
                paymentRepository.save(payment);

        return billingMapper.toPaymentResponse(
                savedPayment
        );
    }

    @Transactional
    public InvoiceResponse completeOnlinePayment(
            String providerOrderCode,
            String transactionReference
    ) {
        Payment payment =
                findByProviderOrderCode(
                        providerOrderCode
                );

        if (payment.getStatus()
                == PaymentStatus.COMPLETED) {
            return billingMapper.toInvoiceResponse(
                    payment.getInvoice()
            );
        }

        if (payment.getStatus()
                != PaymentStatus.PENDING) {
            throw new InvalidInvoiceStateException(
                    "Giao dịch không còn ở trạng thái "
                            + "chờ thanh toán."
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (payment.getExpiresAt() != null
                && !payment.getExpiresAt()
                .isAfter(now)) {
            payment.setStatus(
                    PaymentStatus.EXPIRED
            );

            payment.setFailureReason(
                    "Giao dịch đã hết hạn."
            );

            paymentRepository.save(payment);

            throw new InvalidInvoiceStateException(
                    "Giao dịch đã hết hạn."
            );
        }

        Invoice invoice =
                payment.getInvoice();

        validateInvoiceCanBePaid(invoice);

        validatePaymentAmount(
                invoice,
                payment.getAmount()
        );

        payment.setStatus(
                PaymentStatus.COMPLETED
        );

        payment.setTransactionReference(
                transactionReference
        );

        payment.setPaidAt(now);
        payment.setWebhookReceivedAt(now);
        payment.setFailureReason(null);

        paymentRepository.save(payment);

        applyCompletedPayment(
                invoice,
                payment.getAmount(),
                now
        );

        Invoice savedInvoice =
                invoiceRepository.save(invoice);

        return billingMapper.toInvoiceResponse(
                savedInvoice
        );
    }

    @Transactional
    public PaymentResponse failOnlinePayment(
            String providerOrderCode,
            PaymentStatus newStatus,
            String failureReason
    ) {
        if (newStatus != PaymentStatus.FAILED
                && newStatus
                != PaymentStatus.CANCELLED
                && newStatus
                != PaymentStatus.EXPIRED) {
            throw new IllegalArgumentException(
                    "Trạng thái thất bại không hợp lệ."
            );
        }

        Payment payment =
                findByProviderOrderCode(
                        providerOrderCode
                );

        if (payment.getStatus()
                == PaymentStatus.COMPLETED) {
            throw new InvalidInvoiceStateException(
                    "Không thể chuyển giao dịch đã hoàn thành "
                            + "sang trạng thái thất bại."
            );
        }

        payment.setStatus(newStatus);
        payment.setFailureReason(failureReason);
        payment.setWebhookReceivedAt(
                LocalDateTime.now()
        );

        Payment savedPayment =
                paymentRepository.save(payment);

        return billingMapper.toPaymentResponse(
                savedPayment
        );
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(
            Long paymentId
    ) {
        return billingMapper.toPaymentResponse(
                findPaymentById(paymentId)
        );
    }
    @Transactional(readOnly = true)
    public PaymentResponse
    getPaymentByProviderOrderCode(
            String providerOrderCode
    ) {
        return billingMapper.toPaymentResponse(
                findByProviderOrderCode(
                        providerOrderCode
                )
        );
    }
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(
            Long invoiceId,
            Pageable pageable
    ) {
        if (!invoiceRepository.existsById(
                invoiceId
        )) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy hóa đơn."
            );
        }

        return paymentRepository
                .findByInvoice_Id(
                        invoiceId,
                        pageable
                )
                .map(
                        billingMapper
                                ::toPaymentResponse
                );
    }

    @Transactional(readOnly = true)
    public Payment findPaymentById(
            Long paymentId
    ) {
        return paymentRepository
                .findById(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy giao dịch thanh toán."
                        )
                );
    }

    private Payment findByProviderOrderCode(
            String providerOrderCode
    ) {
        return paymentRepository
                .findByProviderOrderCode(
                        providerOrderCode
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy giao dịch thanh toán."
                        )
                );
    }

    private void expireOrRejectExistingPendingPayment(
            Long invoiceId
    ) {
        paymentRepository
                .findFirstByInvoice_IdAndStatusOrderByCreatedAtDesc(
                        invoiceId,
                        PaymentStatus.PENDING
                )
                .ifPresent(payment -> {
                    LocalDateTime now =
                            LocalDateTime.now();

                    if (payment.getExpiresAt() != null
                            && !payment.getExpiresAt()
                            .isAfter(now)) {
                        payment.setStatus(
                                PaymentStatus.EXPIRED
                        );

                        payment.setFailureReason(
                                "Giao dịch đã hết hạn."
                        );

                        paymentRepository.save(
                                payment
                        );

                        return;
                    }

                    throw new InvalidInvoiceStateException(
                            "Hóa đơn đang có một giao dịch "
                                    + "chờ thanh toán."
                    );
                });
    }

    private void validateCustomerOwnsInvoice(
            Invoice invoice,
            Long customerUserId
    ) {
        if (!invoice.getCustomerUserId()
                .equals(customerUserId)) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy hóa đơn."
            );
        }
    }

    private void validateOnlinePaymentMethod(
            PaymentMethod paymentMethod
    ) {
        if (paymentMethod != PaymentMethod.PAYOS
                && paymentMethod
                != PaymentMethod.ONEPAY) {
            throw new InvalidInvoiceStateException(
                    "Phương thức thanh toán online "
                            + "chỉ hỗ trợ PAYOS hoặc ONEPAY."
            );
        }
    }

    private void validateInvoiceCanBePaid(
            Invoice invoice
    ) {
        InvoiceStatus status =
                invoice.getStatus();

        if (status == InvoiceStatus.DRAFT) {
            throw new InvalidInvoiceStateException(
                    "Hóa đơn phải được phát hành "
                            + "trước khi thanh toán."
            );
        }

        if (status == InvoiceStatus.PAID) {
            throw new InvalidInvoiceStateException(
                    "Hóa đơn đã được thanh toán đủ."
            );
        }

        if (status == InvoiceStatus.CANCELLED) {
            throw new InvalidInvoiceStateException(
                    "Không thể thanh toán hóa đơn đã hủy."
            );
        }

        if (status != InvoiceStatus.ISSUED
                && status
                != InvoiceStatus.PARTIALLY_PAID) {
            throw new InvalidInvoiceStateException(
                    "Trạng thái hóa đơn không cho phép "
                            + "thực hiện thanh toán."
            );
        }
    }

    private void validatePaymentAmount(
            Invoice invoice,
            BigDecimal amount
    ) {
        BigDecimal paidAmount =
                valueOrZero(
                        invoice.getPaidAmount()
                );

        BigDecimal balanceDue =
                invoice.getTotalAmount()
                        .subtract(paidAmount);

        if (amount.compareTo(balanceDue) > 0) {
            throw new InvalidInvoiceStateException(
                    "Số tiền thanh toán vượt quá "
                            + "số tiền còn phải thanh toán: "
                            + balanceDue
                            + "."
            );
        }
    }

    private void applyCompletedPayment(
            Invoice invoice,
            BigDecimal amount,
            LocalDateTime paidAt
    ) {
        BigDecimal newPaidAmount =
                valueOrZero(
                        invoice.getPaidAmount()
                ).add(amount);

        invoice.setPaidAmount(
                newPaidAmount
        );

        if (newPaidAmount.compareTo(
                invoice.getTotalAmount()
        ) == 0) {
            invoice.setStatus(
                    InvoiceStatus.PAID
            );

            invoice.setPaidAt(paidAt);
        }
        else {
            invoice.setStatus(
                    InvoiceStatus.PARTIALLY_PAID
            );
        }
    }

    private String generatePaymentCode() {
        return "PAY-"
                + randomCode();
    }

    private String generateProviderOrderCode(
            PaymentMethod paymentMethod
    ) {
        return paymentMethod.name()
                + "-"
                + randomCode();
    }

    private String randomCode() {
        return UUID.randomUUID()
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