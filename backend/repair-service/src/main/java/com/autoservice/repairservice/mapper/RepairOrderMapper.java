package com.autoservice.repairservice.mapper;

import com.autoservice.repairservice.domain.entity.RepairOrder;
import com.autoservice.repairservice.domain.entity.RepairStatusHistory;
import com.autoservice.repairservice.domain.entity.RepairTask;
import com.autoservice.repairservice.dto.response.RepairOrderResponse;
import com.autoservice.repairservice.dto.response.RepairStatusHistoryResponse;
import com.autoservice.repairservice.dto.response.RepairTaskResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RepairOrderMapper {

    public RepairOrderResponse toResponse(
            RepairOrder repairOrder,
            List<RepairTask> tasks,
            List<RepairStatusHistory> statusHistory
    ) {
        List<RepairTaskResponse> taskResponses =
                tasks == null
                        ? List.of()
                        : tasks.stream()
                        .map(this::toTaskResponse)
                        .toList();

        List<RepairStatusHistoryResponse>
                historyResponses =
                statusHistory == null
                        ? List.of()
                        : statusHistory.stream()
                        .map(this::toHistoryResponse)
                        .toList();

        return new RepairOrderResponse(
                repairOrder.getId(),
                repairOrder.getRepairCode(),
                repairOrder.getBookingId(),
                repairOrder.getCustomerUserId(),
                repairOrder.getVehicleId(),
                repairOrder.getMechanicUserId(),
                repairOrder.getCreatedByUserId(),
                repairOrder.getServiceType(),
                repairOrder.getCustomerComplaint(),
                repairOrder.getDiagnosis(),
                repairOrder.getTechnicianNote(),
                repairOrder.getOdometer(),
                repairOrder.getStatus(),
                repairOrder.getReceivedAt(),
                repairOrder.getEstimatedCompletionAt(),
                repairOrder.getStartedAt(),
                repairOrder.getCompletedAt(),
                repairOrder.getDeliveredAt(),
                repairOrder.getCreatedAt(),
                repairOrder.getUpdatedAt(),
                taskResponses,
                historyResponses
        );
    }

    public RepairTaskResponse toTaskResponse(
            RepairTask task
    ) {
        return new RepairTaskResponse(
                task.getId(),
                task.getTaskName(),
                task.getDescription(),
                task.getStatus(),
                task.getLaborHours(),
                task.getLaborCost(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    public RepairStatusHistoryResponse toHistoryResponse(
            RepairStatusHistory history
    ) {
        return new RepairStatusHistoryResponse(
                history.getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedByUserId(),
                history.getNote(),
                history.getChangedAt()
        );
    }
}