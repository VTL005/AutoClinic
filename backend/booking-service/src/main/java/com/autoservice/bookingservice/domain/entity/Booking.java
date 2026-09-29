package com.autoservice.bookingservice.domain.entity;

import com.autoservice.bookingservice.domain.enums.BookingStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "booking_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String bookingCode;

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
            name = "service_type",
            nullable = false,
            length = 100
    )
    private String serviceType;

    @Column(
            name = "requested_date",
            nullable = false
    )
    private LocalDate requestedDate;

    @Column(
            name = "requested_time",
            nullable = false
    )
    private LocalTime requestedTime;

    @Column(name = "scheduled_start_at")
    private LocalDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at")
    private LocalDateTime scheduledEndAt;

    @Column(
            name = "customer_note",
            length = 1000
    )
    private String customerNote;

    @Column(
            name = "internal_note",
            length = 1000
    )
    private String internalNote;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private BookingStatus status =
            BookingStatus.PENDING;

    @Column(
            name = "cancellation_reason",
            length = 500
    )
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

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

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;
}