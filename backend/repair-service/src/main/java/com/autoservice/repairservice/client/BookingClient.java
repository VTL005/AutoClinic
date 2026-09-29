package com.autoservice.repairservice.client;

import com.autoservice.repairservice.client.dto.BookingDetailsResponse;
import com.autoservice.repairservice.common.ApiResponse;
import com.autoservice.repairservice.exception.ResourceNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class BookingClient {

    private final RestClient bookingRestClient;

    public BookingClient(
            RestClient bookingRestClient
    ) {
        this.bookingRestClient = bookingRestClient;
    }

    public BookingDetailsResponse getBooking(
            Long bookingId,
            String authorizationHeader
    ) {
        try {
            ApiResponse<BookingDetailsResponse> response =
                    bookingRestClient
                            .get()
                            .uri(
                                    "/api/v1/admin/bookings/{bookingId}",
                                    bookingId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorizationHeader
                            )
                            .retrieve()
                            .body(
                                    new ParameterizedTypeReference<>() {
                                    }
                            );

            if (response == null
                    || !response.success()
                    || response.data() == null) {
                throw new ResourceNotFoundException(
                        "Không tìm thấy lịch hẹn."
                );
            }

            return response.data();
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy lịch hẹn."
            );
        }
    }
}