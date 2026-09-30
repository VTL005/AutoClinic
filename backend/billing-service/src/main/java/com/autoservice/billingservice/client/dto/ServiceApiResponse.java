package com.autoservice.billingservice.client.dto;

import java.time.Instant;

public record ServiceApiResponse<T>(

        boolean success,

        String message,

        T data,

        Instant timestamp
) {
}