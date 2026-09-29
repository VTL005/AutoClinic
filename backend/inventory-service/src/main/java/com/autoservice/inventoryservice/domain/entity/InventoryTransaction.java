package com.autoservice.inventoryservice.domain.entity;

import com.autoservice.inventoryservice.domain.enums.InventoryTransactionType;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "part_id",
            nullable = false
    )
    private Part part;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "transaction_type",
            nullable = false,
            length = 30
    )
    private InventoryTransactionType transactionType;

    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;

    @Column(
            name = "quantity_before",
            nullable = false
    )
    private Integer quantityBefore;

    @Column(
            name = "quantity_after",
            nullable = false
    )
    private Integer quantityAfter;

    @Column(
            name = "reference_type",
            length = 50
    )
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(
            name = "note",
            length = 1000
    )
    private String note;

    @Column(
            name = "performed_by_user_id",
            nullable = false
    )
    private Long performedByUserId;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}