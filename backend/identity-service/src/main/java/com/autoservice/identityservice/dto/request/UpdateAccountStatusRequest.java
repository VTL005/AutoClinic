package com.autoservice.identityservice.dto.request;

import com.autoservice.identityservice.domain.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(

        @NotNull(message = "Trạng thái tài khoản không được để trống")
        AccountStatus accountStatus

) {
}