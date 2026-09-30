package com.autoservice.billingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient repairRestClient(
            @Value(
                    "${app.services.repair.base-url}"
            )
            String repairServiceBaseUrl
    ) {
        return RestClient
                .builder()
                .baseUrl(repairServiceBaseUrl)
                .build();
    }
}