package com.autoservice.bookingservice.controller;

import com.autoservice.bookingservice.common.ApiResponse;
import com.autoservice.bookingservice.domain.enums.BookingStatus;
import com.autoservice.bookingservice.dto.request.ConfirmBookingRequest;
import com.autoservice.bookingservice.dto.request.RejectBookingRequest;
import com.autoservice.bookingservice.dto.request.UpdateBookingStatusRequest;
import com.autoservice.bookingservice.dto.response.BookingResponse;
import com.autoservice.bookingservice.service.BookingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ApiResponse<Page<BookingResponse>>
    getAllBookings(
            @RequestParam(required = false)
            BookingStatus status,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = DESC
            )
            Pageable pageable
    ) {
        Page<BookingResponse> response =
                bookingService.getAllBookings(
                        status,
                        pageable
                );

        return ApiResponse.success(
                "Lấy danh sách lịch hẹn thành công.",
                response
        );
    }
    @GetMapping("/calendar")
    public ApiResponse<List<BookingResponse>>
    getCalendarBookings(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate from,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate to,

            @RequestParam(required = false)
            Long mechanicUserId
    ) {
        List<BookingResponse> response =
                bookingService.getCalendarBookings(
                        from,
                        to,
                        mechanicUserId
                );

        return ApiResponse.success(
                "Lấy lịch làm việc thành công.",
                response
        );
    }
    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse>
    getBookingById(
            @PathVariable Long bookingId
    ) {
        BookingResponse response =
                bookingService.getBookingById(
                        bookingId
                );

        return ApiResponse.success(
                "Lấy thông tin lịch hẹn thành công.",
                response
        );
    }

    @PatchMapping("/{bookingId}/confirm")
    public ApiResponse<BookingResponse>
    confirmBooking(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long bookingId,

            @Valid @RequestBody
            ConfirmBookingRequest request
    ) {
        BookingResponse response =
                bookingService.confirmBooking(
                        bookingId,
                        jwt.getTokenValue(),
                        request
                );

        return ApiResponse.success(
                "Xác nhận và phân công lịch hẹn thành công.",
                response
        );
    }

    @PatchMapping("/{bookingId}/reject")
    public ApiResponse<BookingResponse>
    rejectBooking(
            @PathVariable Long bookingId,

            @Valid @RequestBody
            RejectBookingRequest request
    ) {
        BookingResponse response =
                bookingService.rejectBooking(
                        bookingId,
                        request
                );

        return ApiResponse.success(
                "Từ chối lịch hẹn thành công.",
                response
        );
    }

    @PatchMapping("/{bookingId}/status")
    public ApiResponse<BookingResponse>
    updateBookingStatus(
            @PathVariable Long bookingId,

            @Valid @RequestBody
            UpdateBookingStatusRequest request
    ) {
        BookingResponse response =
                bookingService.updateBookingStatus(
                        bookingId,
                        request
                );

        return ApiResponse.success(
                "Cập nhật trạng thái lịch hẹn thành công.",
                response
        );
    }
}