package com.autoservice.bookingservice.mapper;

import com.autoservice.bookingservice.domain.entity.Booking;
import com.autoservice.bookingservice.dto.response.BookingResponse;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(
            Booking booking
    ) {
        return new BookingResponse(
                booking.getId(),
                booking.getBookingCode(),
                booking.getCustomerUserId(),
                booking.getVehicleId(),
                booking.getMechanicUserId(),
                booking.getServiceType(),
                booking.getRequestedDate(),
                booking.getRequestedTime(),
                booking.getScheduledStartAt(),
                booking.getScheduledEndAt(),
                booking.getCustomerNote(),
                booking.getInternalNote(),
                booking.getStatus(),
                booking.getCancellationReason(),
                booking.getCancelledAt(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}