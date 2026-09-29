package com.autoservice.inventoryservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                Map.of(),
                request
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "DUPLICATE_RESOURCE",
                exception.getMessage(),
                Map.of(),
                request
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse>
    handleInsufficientStock(
            InsufficientStockException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "INSUFFICIENT_STOCK",
                exception.getMessage(),
                Map.of(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Dữ liệu gửi lên không hợp lệ.",
                fieldErrors,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        String traceId =
                UUID.randomUUID().toString();

        log.error(
                "Unexpected error. traceId={}, path={}",
                traceId,
                request.getRequestURI(),
                exception
        );

        ErrorResponse response =
                new ErrorResponse(
                        false,
                        "INTERNAL_SERVER_ERROR",
                        "Đã xảy ra lỗi trong quá trình xử lý yêu cầu.",
                        Map.of(),
                        request.getRequestURI(),
                        traceId,
                        Instant.now()
                );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(response);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            Map<String, String> fieldErrors,
            HttpServletRequest request
    ) {
        ErrorResponse response =
                new ErrorResponse(
                        false,
                        errorCode,
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