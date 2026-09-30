package com.autoservice.billingservice.service;

import com.autoservice.billingservice.domain.entity.Payment;
import com.autoservice.billingservice.domain.enums.PaymentStatus;
import com.autoservice.billingservice.dto.response.PaymentResponse;
import com.autoservice.billingservice.exception.ResourceNotFoundException;
import com.autoservice.billingservice.mapper.BillingMapper;
import com.autoservice.billingservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerPaymentService {

    private final PaymentRepository paymentRepository;

    private final BillingMapper billingMapper;

    @Transactional
    public PaymentResponse getPaymentStatus(
            Long paymentId,
            Long customerUserId
    ) {
        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy giao dịch thanh toán."
                                )
                        );

        if (!payment.getInvoice()
                .getCustomerUserId()
                .equals(customerUserId)) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy giao dịch thanh toán."
            );
        }

        expirePaymentIfNecessary(payment);

        return billingMapper.toPaymentResponse(
                payment
        );
    }

    private void expirePaymentIfNecessary(
            Payment payment
    ) {
        if (payment.getStatus()
                != PaymentStatus.PENDING) {
            return;
        }

        if (payment.getExpiresAt() == null) {
            return;
        }

        if (payment.getExpiresAt()
                .isAfter(LocalDateTime.now())) {
            return;
        }

        payment.setStatus(
                PaymentStatus.EXPIRED
        );

        payment.setFailureReason(
                "Giao dịch đã hết hạn."
        );

        paymentRepository.save(payment);
    }
}