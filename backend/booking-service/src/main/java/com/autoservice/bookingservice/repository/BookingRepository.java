package com.autoservice.bookingservice.repository;

import com.autoservice.bookingservice.domain.entity.Booking;
import com.autoservice.bookingservice.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(
            String bookingCode
    );

    Optional<Booking> findByIdAndCustomerUserId(
            Long id,
            Long customerUserId
    );

    Page<Booking> findByCustomerUserId(
            Long customerUserId,
            Pageable pageable
    );

    Page<Booking> findByMechanicUserId(
            Long mechanicUserId,
            Pageable pageable
    );

    Page<Booking> findByStatus(
            BookingStatus status,
            Pageable pageable
    );

    boolean existsByBookingCode(
            String bookingCode
    );

    boolean existsByMechanicUserIdAndStatusInAndScheduledStartAtLessThanAndScheduledEndAtGreaterThan(
            Long mechanicUserId,
            Collection<BookingStatus> statuses,
            LocalDateTime scheduledEndAt,
            LocalDateTime scheduledStartAt
    );
    @Query("""
        SELECT b
        FROM Booking b
        WHERE (
            b.scheduledStartAt IS NOT NULL
            AND b.scheduledStartAt >= :startAt
            AND b.scheduledStartAt < :endAt
        )
        OR (
            b.scheduledStartAt IS NULL
            AND b.requestedDate >= :fromDate
            AND b.requestedDate <= :toDate
        )
        """)
    List<Booking> findCalendarBookings(
            @Param("startAt")
            LocalDateTime startAt,

            @Param("endAt")
            LocalDateTime endAt,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate
    );
}
