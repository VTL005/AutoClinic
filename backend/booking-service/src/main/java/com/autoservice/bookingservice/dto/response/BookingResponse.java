package com.autoservice.bookingservice.dto.response;

import com.autoservice.bookingservice.domain.enums.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BookingResponse(

        Long id,

        String bookingCode,

        Long customerUserId,

        Long vehicleId,

        Long mechanicUserId,

        String serviceType,

        LocalDate requestedDate,

        LocalTime requestedTime,

        LocalDateTime scheduledStartAt,

        LocalDateTime scheduledEndAt,

        String customerNote,

        String internalNote,

        BookingStatus status,

        String cancellationReason,

        LocalDateTime cancelledAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}