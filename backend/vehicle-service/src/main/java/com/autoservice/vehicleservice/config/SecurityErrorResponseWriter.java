package com.autoservice.vehicleservice.config;

import com.autoservice.vehicleservice.common.ErrorResponse;
import com.autoservice.vehicleservice.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final ObjectMapper objectMapper;

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            ErrorCode errorCode,
            String message
    ) throws IOException {

        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.name(),
                message,
                Map.of(),
                request.getRequestURI(),
                traceId
        );

        response.setStatus(status.value());
        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );
        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );
        response.setHeader(
                TRACE_ID_HEADER,
                traceId
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}