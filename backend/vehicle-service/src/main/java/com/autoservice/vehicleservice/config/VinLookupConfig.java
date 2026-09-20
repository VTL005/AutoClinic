package com.autoservice.vehicleservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(VinLookupProperties.class)
public class VinLookupConfig {

    @Bean
    public RestClient nhtsaRestClient(
            VinLookupProperties properties
    ) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(
                        "Accept",
                        "application/json"
                )
                .build();
    }
}