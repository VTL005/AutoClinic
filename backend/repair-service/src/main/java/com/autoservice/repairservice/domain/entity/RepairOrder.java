package com.autoservice.repairservice.domain.entity;

import com.autoservice.repairservice.domain.enums.RepairOrderStatus;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "repair_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "repair_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String repairCode;

    @Column(
            name = "booking_id",
            nullable = false,
            unique = true
    )
    private Long bookingId;

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

    @Column(name = "mechanic_user_id")
    private Long mechanicUserId;

    @Column(
            name = "created_by_user_id",
            nullable = false
    )
    private Long createdByUserId;

    @Column(
            name = "service_type",
            nullable = false,
            length = 200
    )
    private String serviceType;

    @Column(
            name = "customer_complaint",
            length = 1000
    )
    private String customerComplaint;

    @Column(
            name = "diagnosis",
            length = 2000
    )
    private String diagnosis;

    @Column(
            name = "technician_note",
            length = 2000
    )
    private String technicianNote;

    @Column(name = "odometer")
    private Integer odometer;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private RepairOrderStatus status;

    @Column(
            name = "received_at",
            nullable = false
    )
    private LocalDateTime receivedAt;

    @Column(name = "estimated_completion_at")
    private LocalDateTime estimatedCompletionAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

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