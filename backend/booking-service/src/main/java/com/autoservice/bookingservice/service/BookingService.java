package com.autoservice.bookingservice.service;

import com.autoservice.bookingservice.client.VehicleClient;
import com.autoservice.bookingservice.domain.entity.Booking;
import com.autoservice.bookingservice.domain.enums.BookingStatus;
import com.autoservice.bookingservice.dto.request.CancelBookingRequest;
import com.autoservice.bookingservice.dto.request.ConfirmBookingRequest;
import com.autoservice.bookingservice.dto.request.CreateBookingRequest;
import com.autoservice.bookingservice.dto.request.RejectBookingRequest;
import com.autoservice.bookingservice.dto.request.UpdateBookingStatusRequest;
import com.autoservice.bookingservice.dto.response.BookingResponse;
import com.autoservice.bookingservice.exception.InvalidBookingStateException;
import com.autoservice.bookingservice.exception.ResourceNotFoundException;
import com.autoservice.bookingservice.mapper.BookingMapper;
import com.autoservice.bookingservice.repository.BookingRepository;
import com.autoservice.bookingservice.client.IdentityClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final VehicleClient vehicleClient;
    private final IdentityClient identityClient;

    @Transactional
    public BookingResponse createBooking(
            Long customerUserId,
            String accessToken,
            CreateBookingRequest request
    ) {
        validateRequestedSchedule(request);

        vehicleClient.validateVehicleOwnership(
                request.vehicleId(),
                accessToken
        );

        Booking booking = Booking.builder()
                .bookingCode(generateBookingCode())
                .customerUserId(customerUserId)
                .vehicleId(request.vehicleId())
                .serviceType(request.serviceType().trim())
                .requestedDate(request.requestedDate())
                .requestedTime(request.requestedTime())
                .customerNote(normalizeNullableText(
                        request.customerNote()
                ))
                .status(BookingStatus.PENDING)
                .build();

        Booking savedBooking =
                bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getCustomerBooking(
            Long bookingId,
            Long customerUserId
    ) {
        Booking booking = bookingRepository
                .findByIdAndCustomerUserId(
                        bookingId,
                        customerUserId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy lịch hẹn."
                        )
                );

        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getCustomerBookings(
            Long customerUserId,
            Pageable pageable
    ) {
        return bookingRepository
                .findByCustomerUserId(
                        customerUserId,
                        pageable
                )
                .map(bookingMapper::toResponse);
    }

    @Transactional
    public BookingResponse cancelBooking(
            Long bookingId,
            Long customerUserId,
            CancelBookingRequest request
    ) {
        Booking booking = bookingRepository
                .findByIdAndCustomerUserId(
                        bookingId,
                        customerUserId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy lịch hẹn."
                        )
                );

        if (booking.getStatus() != BookingStatus.PENDING
                && booking.getStatus()
                != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException(
                    "Chỉ có thể hủy lịch hẹn đang chờ "
                            + "hoặc đã được xác nhận."
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(
                request.cancellationReason().trim()
        );
        booking.setCancelledAt(LocalDateTime.now());

        return bookingMapper.toResponse(
                bookingRepository.save(booking)
        );
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(
            BookingStatus status,
            Pageable pageable
    ) {
        Page<Booking> bookings;

        if (status == null) {
            bookings = bookingRepository.findAll(pageable);
        } else {
            bookings = bookingRepository.findByStatus(
                    status,
                    pageable
            );
        }

        return bookings.map(bookingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(
            Long bookingId
    ) {
        Booking booking = findBookingOrThrow(bookingId);

        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse confirmBooking(
            Long bookingId,
            String accessToken,
            ConfirmBookingRequest request
    ){
        Booking booking = findBookingOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingStateException(
                    "Chỉ có thể xác nhận lịch hẹn đang chờ."
            );
        }

        validateConfirmedSchedule(request);
        identityClient.validateActiveMechanic(
                request.mechanicUserId(),
                accessToken
        );

        Set<BookingStatus> activeStatuses = Set.of(
                BookingStatus.CONFIRMED,
                BookingStatus.IN_PROGRESS
        );

        boolean hasConflict = bookingRepository
                .existsByMechanicUserIdAndStatusInAndScheduledStartAtLessThanAndScheduledEndAtGreaterThan(
                        request.mechanicUserId(),
                        activeStatuses,
                        request.scheduledEndAt(),
                        request.scheduledStartAt()
                );

        if (hasConflict) {
            throw new InvalidBookingStateException(
                    "Thợ máy đã có lịch trong khoảng thời gian này."
            );
        }

        booking.setMechanicUserId(
                request.mechanicUserId()
        );
        booking.setScheduledStartAt(
                request.scheduledStartAt()
        );
        booking.setScheduledEndAt(
                request.scheduledEndAt()
        );
        booking.setInternalNote(
                normalizeNullableText(
                        request.internalNote()
                )
        );
        booking.setStatus(BookingStatus.CONFIRMED);

        return bookingMapper.toResponse(
                bookingRepository.save(booking)
        );
    }

    @Transactional
    public BookingResponse rejectBooking(
            Long bookingId,
            RejectBookingRequest request
    ) {
        Booking booking = findBookingOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingStateException(
                    "Chỉ có thể từ chối lịch hẹn đang chờ."
            );
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setInternalNote(
                "Lý do từ chối: "
                        + request.reason().trim()
        );

        return bookingMapper.toResponse(
                bookingRepository.save(booking)
        );
    }

    @Transactional
    public BookingResponse updateBookingStatus(
            Long bookingId,
            UpdateBookingStatusRequest request
    ) {
        Booking booking = findBookingOrThrow(bookingId);

        BookingStatus currentStatus =
                booking.getStatus();

        BookingStatus newStatus =
                request.status();

        boolean validTransition =
                (currentStatus == BookingStatus.CONFIRMED
                        && newStatus
                        == BookingStatus.IN_PROGRESS)
                        ||
                        (currentStatus
                                == BookingStatus.IN_PROGRESS
                                && newStatus
                                == BookingStatus.COMPLETED);

        if (!validTransition) {
            throw new InvalidBookingStateException(
                    "Không thể chuyển trạng thái từ "
                            + currentStatus
                            + " sang "
                            + newStatus
                            + "."
            );
        }

        booking.setStatus(newStatus);

        if (request.internalNote() != null) {
            booking.setInternalNote(
                    normalizeNullableText(
                            request.internalNote()
                    )
            );
        }

        return bookingMapper.toResponse(
                bookingRepository.save(booking)
        );
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getCalendarBookings(
            LocalDate from,
            LocalDate to,
            Long mechanicUserId
    ) {
        validateCalendarRange(from, to);

        LocalDateTime startAt =
                from.atStartOfDay();

        LocalDateTime endAt =
                to.plusDays(1).atStartOfDay();

        return bookingRepository
                .findCalendarBookings(
                        startAt,
                        endAt,
                        from,
                        to
                )
                .stream()
                .filter(booking ->
                        mechanicUserId == null
                                || mechanicUserId.equals(
                                booking.getMechanicUserId()
                        )
                )
                .sorted(Comparator.comparing(
                        this::getEffectiveDateTime
                ))
                .map(bookingMapper::toResponse)
                .toList();
    }

    private void validateCalendarRange(
            LocalDate from,
            LocalDate to
    ) {
        if (from == null || to == null) {
            throw new InvalidBookingStateException(
                    "Ngày bắt đầu và ngày kết thúc "
                            + "không được để trống."
            );
        }

        if (to.isBefore(from)) {
            throw new InvalidBookingStateException(
                    "Ngày kết thúc phải bằng "
                            + "hoặc sau ngày bắt đầu."
            );
        }

        if (to.isAfter(from.plusDays(31))) {
            throw new InvalidBookingStateException(
                    "Chỉ được xem lịch trong phạm vi "
                            + "tối đa 31 ngày."
            );
        }
    }

    private LocalDateTime getEffectiveDateTime(
            Booking booking
    ) {
        if (booking.getScheduledStartAt() != null) {
            return booking.getScheduledStartAt();
        }

        return LocalDateTime.of(
                booking.getRequestedDate(),
                booking.getRequestedTime()
        );
    }

    private Booking findBookingOrThrow(
            Long bookingId
    ) {
        return bookingRepository
                .findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy lịch hẹn."
                        )
                );
    }

    private void validateConfirmedSchedule(
            ConfirmBookingRequest request
    ) {
        if (!request.scheduledEndAt().isAfter(
                request.scheduledStartAt()
        )) {
            throw new InvalidBookingStateException(
                    "Thời gian kết thúc phải "
                            + "sau thời gian bắt đầu."
            );
        }

        if (!request.scheduledStartAt().isAfter(
                LocalDateTime.now()
        )) {
            throw new InvalidBookingStateException(
                    "Thời gian bắt đầu phải nằm trong tương lai."
            );
        }
    }

    private void validateRequestedSchedule(
            CreateBookingRequest request
    ) {
        LocalDateTime requestedDateTime =
                LocalDateTime.of(
                        request.requestedDate(),
                        request.requestedTime()
                );

        if (requestedDateTime.isBefore(
                LocalDateTime.now()
        )) {
            throw new InvalidBookingStateException(
                    "Thời gian đặt lịch phải nằm trong tương lai."
            );
        }

        if (request.requestedDate().isAfter(
                LocalDate.now().plusMonths(6)
        )) {
            throw new InvalidBookingStateException(
                    "Chỉ được đặt lịch trước tối đa 6 tháng."
            );
        }
    }

    private String generateBookingCode() {
        String bookingCode;

        do {
            String randomPart = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();

            bookingCode = "BK-" + randomPart;
        } while (
                bookingRepository.existsByBookingCode(
                        bookingCode
                )
        );

        return bookingCode;
    }

    private String normalizeNullableText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}