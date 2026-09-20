package com.autoservice.vehicleservice.service;

import com.autoservice.vehicleservice.config.VinLookupProperties;
import com.autoservice.vehicleservice.domain.entity.VinLookupCache;
import com.autoservice.vehicleservice.dto.response.VinLookupResponse;
import com.autoservice.vehicleservice.exception.BusinessException;
import com.autoservice.vehicleservice.exception.ErrorCode;
import com.autoservice.vehicleservice.exception.ResourceNotFoundException;
import com.autoservice.vehicleservice.integration.nhtsa.NhtsaVinClient;
import com.autoservice.vehicleservice.integration.nhtsa.NhtsaVinResponse;
import com.autoservice.vehicleservice.repository.VinLookupCacheRepository;
import com.autoservice.vehicleservice.util.VinUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VinLookupService {

    private static final String PROVIDER = "NHTSA_VPIC";

    private final VinLookupCacheRepository cacheRepository;
    private final NhtsaVinClient nhtsaVinClient;
    private final VinLookupProperties properties;
    private final ObjectMapper objectMapper;

    public VinLookupService(
            VinLookupCacheRepository cacheRepository,
            NhtsaVinClient nhtsaVinClient,
            VinLookupProperties properties,
            ObjectMapper objectMapper
    ) {
        this.cacheRepository = cacheRepository;
        this.nhtsaVinClient = nhtsaVinClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public VinLookupResponse lookup(String inputVin) {
        String vin = normalizeAndValidate(inputVin);
        Instant currentTime = Instant.now();

        return cacheRepository
                .findByVinAndExpiresAtAfter(
                        vin,
                        currentTime
                )
                .map(cache ->
                        toResponse(cache, true)
                )
                .orElseGet(() ->
                        fetchAndCache(vin, currentTime)
                );
    }

    private VinLookupResponse fetchAndCache(
            String vin,
            Instant fetchedAt
    ) {
        NhtsaVinResponse apiResponse =
                nhtsaVinClient.decodeVin(vin);

        NhtsaVinResponse.VinResult result =
                extractResult(apiResponse);

        Instant expiresAt = fetchedAt.plus(
                properties.cacheTtl()
        );

        VinLookupCache cache = cacheRepository
                .findByVin(vin)
                .orElseGet(VinLookupCache::new);

        cache.setVin(vin);
        cache.setManufacturer(clean(result.Make()));
        cache.setModel(clean(result.Model()));
        cache.setModelYear(
                parseInteger(result.ModelYear())
        );
        cache.setVehicleType(
                clean(result.VehicleType())
        );
        cache.setBodyClass(
                clean(result.BodyClass())
        );
        cache.setEngineDescription(
                buildEngineDescription(result)
        );
        cache.setFuelType(
                clean(result.FuelTypePrimary())
        );
        cache.setPlantCountry(
                clean(result.PlantCountry())
        );
        cache.setPlantCompanyName(
                clean(result.PlantCompanyName())
        );
        cache.setProvider(PROVIDER);
        cache.setRawResponse(
                serializeResponse(apiResponse)
        );
        cache.setFetchedAt(fetchedAt);
        cache.setExpiresAt(expiresAt);

        VinLookupCache savedCache =
                cacheRepository.saveAndFlush(cache);

        return toResponse(savedCache, false);
    }

    private NhtsaVinResponse.VinResult extractResult(
            NhtsaVinResponse response
    ) {
        if (response.Results() == null
                || response.Results().isEmpty()) {
            throw new ResourceNotFoundException(
                    ErrorCode.VIN_INFORMATION_NOT_FOUND,
                    "Không tìm thấy thông tin cho số VIN này."
            );
        }

        NhtsaVinResponse.VinResult result =
                response.Results().get(0);

        String errorCode = clean(result.ErrorCode());

        if (errorCode != null
                && !errorCode.equals("0")) {
            throw new ResourceNotFoundException(
                    ErrorCode.VIN_INFORMATION_NOT_FOUND,
                    buildLookupErrorMessage(result)
            );
        }

        if (clean(result.Make()) == null
                && clean(result.Model()) == null
                && clean(result.ModelYear()) == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.VIN_INFORMATION_NOT_FOUND,
                    "Dịch vụ tra cứu không tìm thấy thông tin xe."
            );
        }

        return result;
    }

    private String normalizeAndValidate(String inputVin) {
        String vin = VinUtils.normalize(inputVin);

        if (!VinUtils.isValid(vin)) {
            throw new BusinessException(
                    ErrorCode.INVALID_VIN,
                    "Số VIN phải gồm đúng 17 ký tự và không chứa I, O hoặc Q."
            );
        }

        return vin;
    }

    private String buildEngineDescription(
            NhtsaVinResponse.VinResult result
    ) {
        String description = Stream.of(
                        clean(result.EngineConfiguration()),
                        formatEngineCylinders(
                                result.EngineCylinders()
                        ),
                        formatDisplacement(
                                result.DisplacementL()
                        )
                )
                .filter(value -> value != null
                        && !value.isBlank())
                .collect(
                        Collectors.joining(", ")
                );

        if (description.isBlank()) {
            return null;
        }

        if (description.length() > 255) {
            return description.substring(0, 255);
        }

        return description;
    }

    private String formatEngineCylinders(
            String cylinders
    ) {
        String value = clean(cylinders);

        if (value == null) {
            return null;
        }

        return value + " cylinders";
    }

    private String formatDisplacement(
            String displacement
    ) {
        String value = clean(displacement);

        if (value == null) {
            return null;
        }

        return value + "L";
    }

    private Integer parseInteger(String value) {
        String normalizedValue = clean(value);

        if (normalizedValue == null) {
            return null;
        }

        try {
            return Integer.valueOf(normalizedValue);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String serializeResponse(
            NhtsaVinResponse response
    ) {
        try {
            return objectMapper.writeValueAsString(
                    response
            );
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.VIN_LOOKUP_FAILED,
                    "Không thể xử lý dữ liệu trả về từ dịch vụ VIN.",
                    exception
            );
        }
    }

    private String buildLookupErrorMessage(
            NhtsaVinResponse.VinResult result
    ) {
        String errorText = clean(result.ErrorText());

        if (errorText == null) {
            return "Không tìm thấy thông tin cho số VIN này.";
        }

        return "Không thể giải mã số VIN: " + errorText;
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private VinLookupResponse toResponse(
            VinLookupCache cache,
            boolean cached
    ) {
        return new VinLookupResponse(
                cache.getVin(),
                cache.getManufacturer(),
                cache.getModel(),
                cache.getModelYear(),
                cache.getVehicleType(),
                cache.getBodyClass(),
                cache.getEngineDescription(),
                cache.getFuelType(),
                cache.getPlantCountry(),
                cache.getPlantCompanyName(),
                cache.getProvider(),
                cached,
                cache.getFetchedAt(),
                cache.getExpiresAt()
        );
    }
}