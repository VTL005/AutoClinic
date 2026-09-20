package com.autoservice.vehicleservice.integration.nhtsa;

import com.autoservice.vehicleservice.exception.BusinessException;
import com.autoservice.vehicleservice.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NhtsaVinClient {

    private final RestClient restClient;

    public NhtsaVinClient(
            @Qualifier("nhtsaRestClient")
            RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public NhtsaVinResponse decodeVin(String vin) {
        try {
            NhtsaVinResponse response =
                    restClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path(
                                            "/DecodeVinValues/{vin}"
                                    )
                                    .queryParam(
                                            "format",
                                            "json"
                                    )
                                    .build(vin)
                            )
                            .retrieve()
                            .body(NhtsaVinResponse.class);

            if (response == null) {
                throw new BusinessException(
                        ErrorCode.VIN_LOOKUP_FAILED,
                        "Dịch vụ tra cứu VIN không trả về dữ liệu."
                );
            }

            return response;

        } catch (BusinessException exception) {
            throw exception;

        } catch (RestClientException exception) {
            throw new BusinessException(
                    ErrorCode.VIN_LOOKUP_FAILED,
                    "Không thể kết nối đến dịch vụ tra cứu VIN.",
                    exception
            );
        }
    }
}