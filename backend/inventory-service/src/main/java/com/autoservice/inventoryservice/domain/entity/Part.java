package com.autoservice.inventoryservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

@Entity
@Table(name = "parts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "part_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String partCode;

    @Column(
            name = "name",
            nullable = false,
            length = 200
    )
    private String name;

    @Column(
            name = "description",
            length = 1000
    )
    private String description;

    @Column(
            name = "manufacturer",
            length = 150
    )
    private String manufacturer;

    @Column(
            name = "unit",
            nullable = false,
            length = 30
    )
    private String unit;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(
            name = "quantity_in_stock",
            nullable = false
    )
    private Integer quantityInStock;

    @Column(
            name = "minimum_stock",
            nullable = false
    )
    private Integer minimumStock;

    @Column(
            name = "active",
            nullable = false
    )
    private Boolean active;

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
}