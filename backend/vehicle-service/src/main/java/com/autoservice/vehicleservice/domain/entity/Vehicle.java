package com.autoservice.vehicleservice.domain.entity;

import com.autoservice.vehicleservice.domain.enums.FuelType;
import com.autoservice.vehicleservice.domain.enums.TransmissionType;
import com.autoservice.vehicleservice.domain.enums.VehicleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.Instant;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "owner_user_id",
            nullable = false
    )
    private Long ownerUserId;

    @Column(
            name = "vin",
            nullable = false,
            unique = true,
            length = 17,
            columnDefinition = "CHAR(17)"
    )
    private String vin;

    @Column(
            name = "license_plate",
            unique = true,
            length = 20
    )
    private String licensePlate;

    @Column(
            name = "manufacturer",
            nullable = false,
            length = 100
    )
    private String manufacturer;

    @Column(
            name = "model",
            nullable = false,
            length = 100
    )
    private String model;

    @Column(
            name = "manufacture_year",
            nullable = false,
            columnDefinition = "SMALLINT UNSIGNED"
    )
    private Integer manufactureYear;

    @Column(
            name = "color",
            length = 50
    )
    private String color;

    @Column(
            name = "engine_type",
            length = 100
    )
    private String engineType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "fuel_type",
            nullable = false,
            length = 20
    )
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "transmission_type",
            length = 20
    )
    private TransmissionType transmissionType;

    @Builder.Default
    @Column(
            name = "odometer_km",
            nullable = false,
            columnDefinition = "INT UNSIGNED"
    )
    private Long odometerKm = 0L;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Builder.Default
    @Column(
            name = "is_deleted",
            nullable = false
    )
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

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

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;
}