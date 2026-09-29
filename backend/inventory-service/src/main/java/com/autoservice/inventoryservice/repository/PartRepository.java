package com.autoservice.inventoryservice.repository;

import com.autoservice.inventoryservice.domain.entity.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartRepository
        extends JpaRepository<Part, Long>,
        JpaSpecificationExecutor<Part> {

    Optional<Part> findByPartCodeIgnoreCase(
            String partCode
    );

    boolean existsByPartCodeIgnoreCase(
            String partCode
    );

    @Query("""
            SELECT part
            FROM Part part
            WHERE (
                :keyword IS NULL
                OR LOWER(part.partCode)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(part.name)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(part.manufacturer)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (
                :active IS NULL
                OR part.active = :active
            )
            AND (
                :lowStock IS NULL
                OR (
                    :lowStock = TRUE
                    AND part.quantityInStock
                        <= part.minimumStock
                )
                OR (
                    :lowStock = FALSE
                    AND part.quantityInStock
                        > part.minimumStock
                )
            )
            """)
    Page<Part> searchParts(
            @Param("keyword")
            String keyword,

            @Param("active")
            Boolean active,

            @Param("lowStock")
            Boolean lowStock,

            Pageable pageable
    );
}