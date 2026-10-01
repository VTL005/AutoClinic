package com.autoservice.notificationservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse>
    handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult()
                .getFieldErrors()) {

            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Dữ liệu gửi lên không hợp lệ.",
                fieldErrors,
                request
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse>
    handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        HttpStatus status =
                statusFor(exception.getErrorCode());

        return buildResponse(
                status,
                exception.getErrorCode(),
                exception.getMessage(),
                Map.of(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        String traceId = UUID.randomUUID().toString();

        log.error(
                "Unexpected error. traceId={}, path={}",
                traceId,
                request.getRequestURI(),
                exception
        );

        ErrorResponse response =
                new ErrorResponse(
                        false,
                        ErrorCode.INTERNAL_SERVER_ERROR.name(),
                        "Hệ thống xảy ra lỗi. Vui lòng thử lại sau.",
                        Map.of(),
                        request.getRequestURI(),
                        traceId,
                        Instant.now()
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private HttpStatus statusFor(
            ErrorCode errorCode
    ) {
        return switch (errorCode) {
            case VALIDATION_ERROR ->
                    HttpStatus.BAD_REQUEST;

            case NOTIFICATION_NOT_FOUND ->
                    HttpStatus.NOT_FOUND;

            case DUPLICATE_NOTIFICATION ->
                    HttpStatus.CONFLICT;

            case ACCESS_DENIED ->
                    HttpStatus.FORBIDDEN;

            case INTERNAL_SERVER_ERROR ->
                    HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ResponseEntity<ErrorResponse>
    buildResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            Map<String, String> fieldErrors,
            HttpServletRequest request
    ) {
        ErrorResponse response =
                new ErrorResponse(
                        false,
                        errorCode.name(),
                        message,
                        fieldErrors,
                        request.getRequestURI(),
                        UUID.randomUUID().toString(),
                        Instant.now()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}