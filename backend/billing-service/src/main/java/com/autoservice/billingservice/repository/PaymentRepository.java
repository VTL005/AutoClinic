package com.autoservice.billingservice.repository;

import com.autoservice.billingservice.domain.entity.Payment;
import com.autoservice.billingservice.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentCode(
            String paymentCode
    );

    Optional<Payment> findByProviderOrderCode(
            String providerOrderCode
    );

    boolean existsByPaymentCode(
            String paymentCode
    );

    boolean existsByProviderOrderCode(
            String providerOrderCode
    );

    List<Payment> findByInvoice_IdOrderByCreatedAtAsc(
            Long invoiceId
    );

    Optional<Payment>
    findFirstByInvoice_IdAndStatusOrderByCreatedAtDesc(
            Long invoiceId,
            PaymentStatus status
    );

    Page<Payment> findByInvoice_Id(
            Long invoiceId,
            Pageable pageable
    );

    Page<Payment> findByStatus(
            PaymentStatus status,
            Pageable pageable
    );
}