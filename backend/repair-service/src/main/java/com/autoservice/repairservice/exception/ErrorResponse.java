package com.autoservice.repairservice.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(

        boolean success,

        String errorCode,

        String message,

        Map<String, String> fieldErrors,

        String path,

        String traceId,

        Instant timestamp
) {
}