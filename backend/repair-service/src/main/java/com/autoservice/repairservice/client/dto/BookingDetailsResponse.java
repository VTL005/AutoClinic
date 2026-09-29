package com.autoservice.repairservice.client.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BookingDetailsResponse(

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

        String status
) {
}