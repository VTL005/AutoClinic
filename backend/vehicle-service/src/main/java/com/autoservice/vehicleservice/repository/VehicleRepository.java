package com.autoservice.vehicleservice.repository;

import com.autoservice.vehicleservice.domain.entity.Vehicle;
import com.autoservice.vehicleservice.domain.enums.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository
        extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByIdAndDeletedFalse(
            Long id
    );

    Optional<Vehicle> findByIdAndOwnerUserIdAndDeletedFalse(
            Long id,
            Long ownerUserId
    );

    Optional<Vehicle> findByVinIgnoreCaseAndDeletedFalse(
            String vin
    );

    boolean existsByVinIgnoreCase(
            String vin
    );

    boolean existsByLicensePlateIgnoreCase(
            String licensePlate
    );

    Page<Vehicle> findAllByOwnerUserIdAndDeletedFalse(
            Long ownerUserId,
            Pageable pageable
    );

    @Query("""
            SELECT vehicle
            FROM Vehicle vehicle
            WHERE vehicle.deleted = false
              AND (
                    :ownerUserId IS NULL
                    OR vehicle.ownerUserId = :ownerUserId
                  )
              AND (
                    :status IS NULL
                    OR vehicle.status = :status
                  )
              AND (
                    :keyword IS NULL
                    OR LOWER(vehicle.vin)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(vehicle.licensePlate)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(vehicle.manufacturer)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(vehicle.model)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<Vehicle> searchVehicles(
            @Param("ownerUserId") Long ownerUserId,
            @Param("status") VehicleStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}