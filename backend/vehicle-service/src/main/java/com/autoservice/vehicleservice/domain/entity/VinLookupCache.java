package com.autoservice.vehicleservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "vin_lookup_cache")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VinLookupCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "vin",
            nullable = false,
            unique = true,
            length = 17,
            columnDefinition = "CHAR(17)"
    )
    private String vin;

    @Column(
            name = "manufacturer",
            length = 100
    )
    private String manufacturer;

    @Column(
            name = "model",
            length = 100
    )
    private String model;

    @Column(
            name = "model_year",
            columnDefinition = "SMALLINT UNSIGNED"
    )
    private Integer modelYear;

    @Column(
            name = "vehicle_type",
            length = 100
    )
    private String vehicleType;

    @Column(
            name = "body_class",
            length = 100
    )
    private String bodyClass;

    @Column(
            name = "engine_description",
            length = 255
    )
    private String engineDescription;

    @Column(
            name = "fuel_type",
            length = 100
    )
    private String fuelType;

    @Column(
            name = "plant_country",
            length = 100
    )
    private String plantCountry;

    @Column(
            name = "plant_company_name",
            length = 150
    )
    private String plantCompanyName;

    @Column(
            name = "provider",
            nullable = false,
            length = 50
    )
    private String provider;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "raw_response",
            columnDefinition = "JSON"
    )
    private String rawResponse;

    @Column(
            name = "fetched_at",
            nullable = false
    )
    private Instant fetchedAt;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    public boolean isExpired() {
        return expiresAt == null
                || !expiresAt.isAfter(Instant.now());
    }

    public boolean isUsable() {
        return !isExpired();
    }
}