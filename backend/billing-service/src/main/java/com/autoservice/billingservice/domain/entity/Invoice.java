package com.autoservice.billingservice.domain.entity;

import com.autoservice.billingservice.domain.enums.InvoiceStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "invoice_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String invoiceCode;

    @Column(
            name = "repair_order_id",
            nullable = false,
            unique = true
    )
    private Long repairOrderId;

    @Column(
            name = "customer_user_id",
            nullable = false
    )
    private Long customerUserId;

    @Column(
            name = "vehicle_id",
            nullable = false
    )
    private Long vehicleId;

    @Column(
            name = "created_by_user_id",
            nullable = false
    )
    private Long createdByUserId;

    @Column(
            name = "subtotal",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal subtotal;

    @Column(
            name = "discount_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal discountAmount;

    @Column(
            name = "tax_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal taxAmount;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Column(
            name = "paid_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private InvoiceStatus status;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(
            name = "note",
            length = 1000
    )
    private String note;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<InvoiceItem> items =
            new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "invoice",
            fetch = FetchType.LAZY
    )
    private List<Payment> payments =
            new ArrayList<>();
}