package com.autoservice.identityservice.common;

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

    public static ErrorResponse of(
            String errorCode,
            String message,
            Map<String, String> fieldErrors,
            String path,
            String traceId
    ) {
        return new ErrorResponse(
                false,
                errorCode,
                message,
                fieldErrors,
                path,
                traceId,
                Instant.now()
        );
    }
}