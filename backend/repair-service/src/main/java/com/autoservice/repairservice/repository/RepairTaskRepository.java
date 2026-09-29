package com.autoservice.repairservice.repository;

import com.autoservice.repairservice.domain.entity.RepairTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairTaskRepository
        extends JpaRepository<RepairTask, Long> {

    List<RepairTask>
    findByRepairOrder_IdOrderByCreatedAtAsc(
            Long repairOrderId
    );

    Optional<RepairTask> findByIdAndRepairOrder_Id(
            Long id,
            Long repairOrderId
    );
}