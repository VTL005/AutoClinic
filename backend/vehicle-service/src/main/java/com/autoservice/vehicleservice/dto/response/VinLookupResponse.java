package com.autoservice.vehicleservice.dto.response;

import java.time.Instant;

public record VinLookupResponse(
        String vin,
        String manufacturer,
        String model,
        Integer modelYear,
        String vehicleType,
        String bodyClass,
        String engineDescription,
        String fuelType,
        String plantCountry,
        String plantCompanyName,
        String provider,
        boolean cached,
        Instant fetchedAt,
        Instant expiresAt
) {
}