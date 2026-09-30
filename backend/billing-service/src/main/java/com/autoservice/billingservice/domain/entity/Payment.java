package com.autoservice.billingservice.domain.entity;

import com.autoservice.billingservice.domain.enums.PaymentMethod;
import com.autoservice.billingservice.domain.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "payment_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String paymentCode;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "invoice_id",
            nullable = false
    )
    private Invoice invoice;

    @Column(
            name = "amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false,
            length = 40
    )
    private PaymentMethod paymentMethod;

    @Column(
            name = "provider_order_code",
            unique = true,
            length = 100
    )
    private String providerOrderCode;

    @Column(
            name = "checkout_url",
            length = 1000
    )
    private String checkoutUrl;

    @Column(
            name = "qr_code",
            columnDefinition = "TEXT"
    )
    private String qrCode;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private PaymentStatus status;

    @Column(
            name = "transaction_reference",
            length = 150
    )
    private String transactionReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "webhook_received_at")
    private LocalDateTime webhookReceivedAt;

    @Column(
            name = "failure_reason",
            length = 1000
    )
    private String failureReason;

    @Column(
            name = "received_by_user_id",
            nullable = false
    )
    private Long receivedByUserId;

    @Column(
            name = "note",
            length = 1000
    )
    private String note;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}