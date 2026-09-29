package com.autoservice.repairservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient bookingRestClient(
            @Value("${services.booking.base-url}")
            String bookingServiceBaseUrl
    ) {
        return RestClient.builder()
                .baseUrl(bookingServiceBaseUrl)
                .build();
    }
}