package com.autoservice.inventoryservice.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdatePartStatusRequest(

        @NotNull(
                message = "Trạng thái hoạt động không được để trống"
        )
        Boolean active
) {
}