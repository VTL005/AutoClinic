package com.autoservice.vehicleservice.service;

import com.autoservice.vehicleservice.domain.entity.Vehicle;
import com.autoservice.vehicleservice.domain.enums.VehicleStatus;
import com.autoservice.vehicleservice.dto.request.CreateVehicleRequest;
import com.autoservice.vehicleservice.dto.request.UpdateVehicleRequest;
import com.autoservice.vehicleservice.dto.response.PageResponse;
import com.autoservice.vehicleservice.dto.response.VehicleResponse;
import com.autoservice.vehicleservice.exception.BusinessException;
import com.autoservice.vehicleservice.exception.DuplicateResourceException;
import com.autoservice.vehicleservice.exception.ErrorCode;
import com.autoservice.vehicleservice.exception.ResourceNotFoundException;
import com.autoservice.vehicleservice.repository.VehicleRepository;
import com.autoservice.vehicleservice.util.LicensePlateUtils;
import com.autoservice.vehicleservice.util.VinUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private static final int MAX_PAGE_SIZE = 100;

    private final VehicleRepository vehicleRepository;

    @Transactional
    public VehicleResponse createVehicle(
            Long ownerUserId,
            CreateVehicleRequest request
    ) {
        String vin = VinUtils.normalize(request.vin());

        if (!VinUtils.isValid(vin)) {
            throw new BusinessException(
                    ErrorCode.INVALID_VIN,
                    "VIN phải có đúng 17 ký tự và không chứa I, O, Q."
            );
        }

        String licensePlate =
                LicensePlateUtils.normalize(
                        request.licensePlate()
                );

        validateUniqueVin(vin);
        validateUniqueLicensePlate(licensePlate);

        Vehicle vehicle = Vehicle.builder()
                .ownerUserId(ownerUserId)
                .vin(vin)
                .licensePlate(licensePlate)
                .manufacturer(
                        request.manufacturer().trim()
                )
                .model(request.model().trim())
                .manufactureYear(
                        request.manufactureYear()
                )
                .color(normalizeOptionalText(
                        request.color()
                ))
                .engineType(normalizeOptionalText(
                        request.engineType()
                ))
                .fuelType(request.fuelType())
                .transmissionType(
                        request.transmissionType()
                )
                .odometerKm(request.odometerKm())
                .status(VehicleStatus.ACTIVE)
                .deleted(false)
                .build();

        Vehicle savedVehicle =
                vehicleRepository.saveAndFlush(vehicle);

        return toResponse(savedVehicle);
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getMyVehicles(
            Long ownerUserId,
            String keyword,
            VehicleStatus status,
            Pageable pageable
    ) {
        validatePageSize(pageable);

        Page<VehicleResponse> result =
                vehicleRepository.searchVehicles(
                        ownerUserId,
                        status,
                        normalizeKeyword(keyword),
                        pageable
                ).map(this::toResponse);

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getMyVehicle(
            Long ownerUserId,
            Long vehicleId
    ) {
        Vehicle vehicle =
                requireOwnedVehicle(
                        ownerUserId,
                        vehicleId
                );

        return toResponse(vehicle);
    }

    @Transactional
    public VehicleResponse updateMyVehicle(
            Long ownerUserId,
            Long vehicleId,
            UpdateVehicleRequest request
    ) {
        Vehicle vehicle =
                requireOwnedVehicle(
                        ownerUserId,
                        vehicleId
                );

        String newLicensePlate =
                LicensePlateUtils.normalize(
                        request.licensePlate()
                );

        validateLicensePlateForUpdate(
                vehicle,
                newLicensePlate
        );

        vehicle.setLicensePlate(newLicensePlate);
        vehicle.setManufacturer(
                request.manufacturer().trim()
        );
        vehicle.setModel(request.model().trim());
        vehicle.setManufactureYear(
                request.manufactureYear()
        );
        vehicle.setColor(
                normalizeOptionalText(request.color())
        );
        vehicle.setEngineType(
                normalizeOptionalText(
                        request.engineType()
                )
        );
        vehicle.setFuelType(request.fuelType());
        vehicle.setTransmissionType(
                request.transmissionType()
        );
        vehicle.setOdometerKm(
                request.odometerKm()
        );

        Vehicle savedVehicle =
                vehicleRepository.saveAndFlush(vehicle);

        return toResponse(savedVehicle);
    }

    @Transactional
    public void deleteMyVehicle(
            Long ownerUserId,
            Long vehicleId
    ) {
        Vehicle vehicle =
                requireOwnedVehicle(
                        ownerUserId,
                        vehicleId
                );

        vehicle.setDeleted(true);
        vehicle.setDeletedAt(Instant.now());
        vehicle.setStatus(VehicleStatus.INACTIVE);

        vehicleRepository.saveAndFlush(vehicle);
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getVehiclesForAdmin(
            Long ownerUserId,
            String keyword,
            VehicleStatus status,
            Pageable pageable
    ) {
        validatePageSize(pageable);

        Page<VehicleResponse> result =
                vehicleRepository.searchVehicles(
                        ownerUserId,
                        status,
                        normalizeKeyword(keyword),
                        pageable
                ).map(this::toResponse);

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleForAdmin(
            Long vehicleId
    ) {
        Vehicle vehicle = vehicleRepository
                .findByIdAndDeletedFalse(vehicleId)
                .orElseThrow(() ->
                        vehicleNotFound(vehicleId)
                );

        return toResponse(vehicle);
    }

    private Vehicle requireOwnedVehicle(
            Long ownerUserId,
            Long vehicleId
    ) {
        return vehicleRepository
                .findByIdAndOwnerUserIdAndDeletedFalse(
                        vehicleId,
                        ownerUserId
                )
                .orElseThrow(() ->
                        vehicleNotFound(vehicleId)
                );
    }

    private void validateUniqueVin(
            String vin
    ) {
        if (vehicleRepository
                .existsByVinIgnoreCase(vin)) {
            throw new DuplicateResourceException(
                    ErrorCode.VIN_ALREADY_EXISTS,
                    "VIN đã tồn tại trong hệ thống."
            );
        }
    }

    private void validateUniqueLicensePlate(
            String licensePlate
    ) {
        if (licensePlate != null
                && vehicleRepository
                .existsByLicensePlateIgnoreCase(
                        licensePlate
                )) {
            throw new DuplicateResourceException(
                    ErrorCode.LICENSE_PLATE_ALREADY_EXISTS,
                    "Biển số xe đã tồn tại trong hệ thống."
            );
        }
    }

    private void validateLicensePlateForUpdate(
            Vehicle vehicle,
            String newLicensePlate
    ) {
        if (newLicensePlate == null) {
            return;
        }

        String currentLicensePlate =
                LicensePlateUtils.normalize(
                        vehicle.getLicensePlate()
                );

        if (!newLicensePlate.equals(
                currentLicensePlate
        )) {
            validateUniqueLicensePlate(
                    newLicensePlate
            );
        }
    }

    private void validatePageSize(
            Pageable pageable
    ) {
        if (pageable.getPageSize()
                > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Số phần tử mỗi trang không được vượt quá 100."
            );
        }
    }

    private String normalizeKeyword(
            String keyword
    ) {
        if (keyword == null
                || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private ResourceNotFoundException
    vehicleNotFound(
            Long vehicleId
    ) {
        return new ResourceNotFoundException(
                ErrorCode.VEHICLE_NOT_FOUND,
                "Không tìm thấy xe có ID "
                        + vehicleId
                        + "."
        );
    }

    private VehicleResponse toResponse(
            Vehicle vehicle
    ) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getOwnerUserId(),
                vehicle.getVin(),
                vehicle.getLicensePlate(),
                vehicle.getManufacturer(),
                vehicle.getModel(),
                vehicle.getManufactureYear(),
                vehicle.getColor(),
                vehicle.getEngineType(),
                vehicle.getFuelType(),
                vehicle.getTransmissionType(),
                vehicle.getOdometerKm(),
                vehicle.getStatus(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}