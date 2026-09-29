package com.autoservice.bookingservice.client;

import com.autoservice.bookingservice.exception.ExternalServiceException;
import com.autoservice.bookingservice.exception.InvalidBookingStateException;
import com.autoservice.bookingservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class IdentityClient {

    private final RestClient restClient;

    public IdentityClient(
            @Value("${services.identity.base-url}")
            String identityServiceBaseUrl
    ) {
        this.restClient = RestClient
                .builder()
                .baseUrl(identityServiceBaseUrl)
                .build();
    }

    public void validateActiveMechanic(
            Long mechanicUserId,
            String accessToken
    ) {
        try {
            IdentityApiResponse response = restClient
                    .get()
                    .uri(
                            "/api/v1/admin/mechanics/by-user/{userId}",
                            mechanicUserId
                    )
                    .headers(headers ->
                            headers.setBearerAuth(accessToken)
                    )
                    .retrieve()
                    .body(IdentityApiResponse.class);

            if (response == null || response.data() == null) {
                throw new ExternalServiceException(
                        "Identity-service trả về dữ liệu không hợp lệ."
                );
            }

            MechanicData mechanic = response.data();

            if (!"ACTIVE".equals(mechanic.accountStatus())) {
                throw new InvalidBookingStateException(
                        "Tài khoản thợ máy không hoạt động."
                );
            }

            if (!"ACTIVE".equals(mechanic.employmentStatus())) {
                throw new InvalidBookingStateException(
                        "Thợ máy hiện không trong trạng thái làm việc."
                );
            }
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy thợ máy."
            );
        } catch (HttpClientErrorException exception) {
            throw new ExternalServiceException(
                    "Identity-service từ chối yêu cầu xác minh thợ máy."
            );
        } catch (RestClientException exception) {
            throw new ExternalServiceException(
                    "Không thể kết nối tới identity-service."
            );
        }
    }

    private record IdentityApiResponse(
            MechanicData data
    ) {
    }

    private record MechanicData(
            Long userId,
            String accountStatus,
            String employmentStatus
    ) {
    }
}