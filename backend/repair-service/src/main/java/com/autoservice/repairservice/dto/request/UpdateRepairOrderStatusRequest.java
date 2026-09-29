package com.autoservice.repairservice.dto.request;

import com.autoservice.repairservice.domain.enums.RepairOrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRepairOrderStatusRequest(

        @NotNull(
                message = "Trạng thái mới không được để trống"
        )
        RepairOrderStatus status,

        @Size(
                max = 1000,
                message = "Ghi chú trạng thái không được vượt quá "
                        + "1000 ký tự"
        )
        String note
) {
}