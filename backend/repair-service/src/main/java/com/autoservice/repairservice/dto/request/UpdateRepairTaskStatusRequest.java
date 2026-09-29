package com.autoservice.repairservice.dto.request;

import com.autoservice.repairservice.domain.enums.RepairTaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRepairTaskStatusRequest(

        @NotNull(
                message = "Trạng thái công việc không được để trống"
        )
        RepairTaskStatus status
) {
}