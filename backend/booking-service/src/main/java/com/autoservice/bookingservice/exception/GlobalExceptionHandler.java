package com.autoservice.bookingservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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

    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidBookingState(
            InvalidBookingStateException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "INVALID_BOOKING_STATE",
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
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse>
    handleExternalService(
            ExternalServiceException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "EXTERNAL_SERVICE_UNAVAILABLE",
                exception.getMessage(),
                Map.of(),
                request
        );
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Đã xảy ra lỗi trong quá trình xử lý yêu cầu.",
                Map.of(),
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            Map<String, String> fieldErrors,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
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