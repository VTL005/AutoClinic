package com.autoservice.vehicleservice.exception;

import com.autoservice.vehicleservice.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_HEADER =
            "X-Trace-Id";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse>
    handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                statusFor(exception.getErrorCode()),
                exception.getErrorCode(),
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
                "Dữ liệu đầu vào không hợp lệ.",
                fieldErrors,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(violation ->
                        fieldErrors.put(
                                violation.getPropertyPath()
                                        .toString(),
                                violation.getMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Dữ liệu đầu vào không hợp lệ.",
                fieldErrors,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse>
    handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Nội dung JSON không hợp lệ hoặc chứa giá trị không được hỗ trợ.",
                Map.of(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse>
    handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors =
                Map.of(
                        exception.getName(),
                        "Giá trị tham số không hợp lệ."
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Tham số yêu cầu không hợp lệ.",
                fieldErrors,
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                "Bạn không có quyền thực hiện thao tác này.",
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
        log.error(
                "Unexpected error while handling request {}",
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Hệ thống xảy ra lỗi. Vui lòng thử lại sau.",
                Map.of(),
                request
        );
    }

    private HttpStatus statusFor(
            ErrorCode errorCode
    ) {
        return switch (errorCode) {
            case VALIDATION_ERROR,
                 INVALID_VIN ->
                    HttpStatus.BAD_REQUEST;

            case VEHICLE_NOT_FOUND,
                 VIN_INFORMATION_NOT_FOUND ->
                    HttpStatus.NOT_FOUND;

            case VIN_ALREADY_EXISTS,
                 LICENSE_PLATE_ALREADY_EXISTS ->
                    HttpStatus.CONFLICT;

            case VIN_LOOKUP_FAILED ->
                    HttpStatus.BAD_GATEWAY;

            case AUTHENTICATION_REQUIRED ->
                    HttpStatus.UNAUTHORIZED;

            case ACCESS_DENIED ->
                    HttpStatus.FORBIDDEN;

            case INTERNAL_SERVER_ERROR ->
                    HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            Map<String, String> fieldErrors,
            HttpServletRequest request
    ) {
        String traceId = resolveTraceId(request);

        ErrorResponse response = ErrorResponse.of(
                errorCode.name(),
                message,
                fieldErrors,
                request.getRequestURI(),
                traceId
        );

        return ResponseEntity
                .status(status)
                .header(TRACE_ID_HEADER, traceId)
                .body(response);
    }

    private String resolveTraceId(
            HttpServletRequest request
    ) {
        String existingTraceId =
                request.getHeader(TRACE_ID_HEADER);

        if (existingTraceId != null
                && !existingTraceId.isBlank()) {
            return existingTraceId.trim();
        }

        return UUID.randomUUID().toString();
    }
}