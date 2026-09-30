package com.autoservice.billingservice.client;

import com.autoservice.billingservice.client.dto.RepairOrderDetailsResponse;
import com.autoservice.billingservice.client.dto.ServiceApiResponse;
import com.autoservice.billingservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class RepairClient {

    private final RestClient repairRestClient;

    public RepairOrderDetailsResponse getRepairOrder(
            Long repairOrderId,
            String authorizationHeader
    ) {
        try {
            ServiceApiResponse<
                    RepairOrderDetailsResponse
                    > response =
                    repairRestClient
                            .get()
                            .uri(
                                    "/api/v1/repair-orders/{repairOrderId}",
                                    repairOrderId
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
                        "Không tìm thấy phiếu sửa chữa."
                );
            }

            return response.data();
        }
        catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy phiếu sửa chữa."
            );
        }
    }
}