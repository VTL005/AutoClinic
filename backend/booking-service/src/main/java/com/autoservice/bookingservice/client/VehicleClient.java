package com.autoservice.bookingservice.client;

import com.autoservice.bookingservice.exception.ExternalServiceException;
import com.autoservice.bookingservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class VehicleClient {

    private final RestClient restClient;

    public VehicleClient(
            @Value("${services.vehicle.base-url}")
            String vehicleServiceBaseUrl
    ) {
        this.restClient = RestClient
                .builder()
                .baseUrl(vehicleServiceBaseUrl)
                .build();
    }

    public void validateVehicleOwnership(
            Long vehicleId,
            String accessToken
    ) {
        try {
            restClient
                    .get()
                    .uri(
                            "/api/v1/vehicles/{vehicleId}",
                            vehicleId
                    )
                    .headers(headers ->
                            headers.setBearerAuth(accessToken)
                    )
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException(
                        "Không tìm thấy phương tiện "
                                + "thuộc tài khoản khách hàng."
                );
            }

            throw new ExternalServiceException(
                    "Vehicle-service từ chối yêu cầu "
                            + "xác minh phương tiện."
            );
        } catch (RestClientException exception) {
            throw new ExternalServiceException(
                    "Không thể kết nối tới vehicle-service."
            );
        }
    }
}