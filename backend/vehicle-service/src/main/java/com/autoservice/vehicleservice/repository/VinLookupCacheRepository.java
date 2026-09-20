package com.autoservice.vehicleservice.repository;

import com.autoservice.vehicleservice.domain.entity.VinLookupCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VinLookupCacheRepository
        extends JpaRepository<VinLookupCache, Long> {

    Optional<VinLookupCache> findByVin(
            String vin
    );

    Optional<VinLookupCache> findByVinAndExpiresAtAfter(
            String vin,
            Instant currentTime
    );

    boolean existsByVin(
            String vin
    );

    long deleteByExpiresAtBefore(
            Instant currentTime
    );
}