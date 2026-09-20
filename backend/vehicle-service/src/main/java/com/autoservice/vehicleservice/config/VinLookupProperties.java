package com.autoservice.vehicleservice.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.vin")
@Validated
public record VinLookupProperties(

        @NotBlank
        String baseUrl,

        @NotNull
        Duration cacheTtl
) {
}