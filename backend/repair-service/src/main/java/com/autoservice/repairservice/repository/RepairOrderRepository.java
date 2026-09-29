package com.autoservice.repairservice.repository;

import com.autoservice.repairservice.domain.entity.RepairOrder;
import com.autoservice.repairservice.domain.enums.RepairOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepairOrderRepository
        extends JpaRepository<RepairOrder, Long> {

    Optional<RepairOrder> findByRepairCode(
            String repairCode
    );

    Optional<RepairOrder> findByBookingId(
            Long bookingId
    );

    Page<RepairOrder> findByStatus(
            RepairOrderStatus status,
            Pageable pageable
    );

    Page<RepairOrder> findByCustomerUserId(
            Long customerUserId,
            Pageable pageable
    );
    Optional<RepairOrder> findByIdAndCustomerUserId(
            Long id,
            Long customerUserId
    );

    Page<RepairOrder> findByMechanicUserId(
            Long mechanicUserId,
            Pageable pageable
    );
    Optional<RepairOrder> findByIdAndMechanicUserId(
            Long id,
            Long mechanicUserId
    );
    boolean existsByRepairCode(
            String repairCode
    );

    boolean existsByBookingId(
            Long bookingId
    );
}