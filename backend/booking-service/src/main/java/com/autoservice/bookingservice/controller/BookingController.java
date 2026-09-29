package com.autoservice.bookingservice.controller;

import com.autoservice.bookingservice.common.ApiResponse;
import com.autoservice.bookingservice.dto.request.CancelBookingRequest;
import com.autoservice.bookingservice.dto.request.CreateBookingRequest;
import com.autoservice.bookingservice.dto.response.BookingResponse;
import com.autoservice.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>>
    createBooking(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            CreateBookingRequest request
    ) {
        BookingResponse response =
                bookingService.createBooking(
                        getUserId(jwt),
                        jwt.getTokenValue(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Đặt lịch thành công.",
                        response
                ));
    }

    @GetMapping
    public ApiResponse<Page<BookingResponse>>
    getCustomerBookings(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = DESC
            )
            Pageable pageable
    ) {
        Page<BookingResponse> response =
                bookingService.getCustomerBookings(
                        getUserId(jwt),
                        pageable
                );

        return ApiResponse.success(
                "Lấy danh sách lịch hẹn thành công.",
                response
        );
    }

    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> getBooking(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long bookingId
    ) {
        BookingResponse response =
                bookingService.getCustomerBooking(
                        bookingId,
                        getUserId(jwt)
                );

        return ApiResponse.success(
                "Lấy thông tin lịch hẹn thành công.",
                response
        );
    }

    @PatchMapping("/{bookingId}/cancel")
    public ApiResponse<BookingResponse> cancelBooking(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long bookingId,
            @Valid @RequestBody
            CancelBookingRequest request
    ) {
        BookingResponse response =
                bookingService.cancelBooking(
                        bookingId,
                        getUserId(jwt),
                        request
                );

        return ApiResponse.success(
                "Hủy lịch hẹn thành công.",
                response
        );
    }

    private Long getUserId(
            Jwt jwt
    ) {
        return Long.valueOf(jwt.getSubject());
    }
}