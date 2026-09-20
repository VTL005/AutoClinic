package com.autoservice.vehicleservice.dto.response;

import com.autoservice.vehicleservice.domain.enums.FuelType;
import com.autoservice.vehicleservice.domain.enums.TransmissionType;
import com.autoservice.vehicleservice.domain.enums.VehicleStatus;

import java.time.Instant;

public record VehicleResponse(
        Long id,
        Long ownerUserId,
        String vin,
        String licensePlate,
        String manufacturer,
        String model,
        Integer manufactureYear,
        String color,
        String engineType,
        FuelType fuelType,
        TransmissionType transmissionType,
        Long odometerKm,
        VehicleStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}