package com.autoservice.vehicleservice.integration.nhtsa;

import java.util.List;

public record NhtsaVinResponse(
        int Count,
        String Message,
        List<VinResult> Results
) {

    public record VinResult(
            String VIN,
            String Make,
            String Model,
            String ModelYear,
            String VehicleType,
            String BodyClass,
            String EngineConfiguration,
            String EngineCylinders,
            String DisplacementL,
            String FuelTypePrimary,
            String PlantCountry,
            String PlantCompanyName,
            String ErrorCode,
            String ErrorText
    ) {
    }
}