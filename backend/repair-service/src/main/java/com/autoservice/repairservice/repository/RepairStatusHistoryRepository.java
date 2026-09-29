package com.autoservice.repairservice.repository;

import com.autoservice.repairservice.domain.entity.RepairStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairStatusHistoryRepository
        extends JpaRepository<
        RepairStatusHistory,
        Long
        > {

    List<RepairStatusHistory>
    findByRepairOrder_IdOrderByChangedAtAsc(
            Long repairOrderId
    );
}