package com.autoservice.identityservice.exception;

import com.autoservice.identityservice.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    private static final String TRACE_ID_HEADER =
            "X-Trace-Id";

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse>
    handleDuplicateResource(
            DuplicateResourceException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getErrorCode(),
                exception.getMessage(),
                Map.of(),
                request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getErrorCode(),
                exception.getMessage(),
                Map.of(),
                request
        );
    }

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
                : exception.getBindingResult().getFieldErrors()) {

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
                                violation
                                        .getPropertyPath()
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

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateDatabase(
            org.springframework.dao.DataIntegrityViolationException exception,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ErrorCode.VALIDATION_ERROR,
                "Thông tin đã được sử dụng. Kiểm tra số điện thoại, email hoặc tên đăng nhập trước khi gửi lại.", Map.of(), request);
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

    private HttpStatus statusFor(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_CREDENTIALS,
                 INVALID_REFRESH_TOKEN,
                 AUTHENTICATION_REQUIRED ->
                    HttpStatus.UNAUTHORIZED;

            case ACCOUNT_PENDING_ACTIVATION,
                 ACCOUNT_DISABLED,
                 ACCESS_DENIED,
                 CANNOT_UPDATE_OWN_ACCOUNT ->
                    HttpStatus.FORBIDDEN;

            case ACCOUNT_LOCKED ->
                    HttpStatus.LOCKED;

            case USER_NOT_FOUND,
                 MECHANIC_PROFILE_NOT_FOUND ->
                    HttpStatus.NOT_FOUND;

            case USERNAME_ALREADY_EXISTS,
                 PHONE_ALREADY_EXISTS,
                 EMAIL_ALREADY_EXISTS ->
                    HttpStatus.CONFLICT;

            case ACTIVATION_CODE_INVALID,
                 VALIDATION_ERROR,
                 INVALID_ACCOUNT_STATUS,
                 PROFILE_UPDATE_NO_CHANGES,
                 CURRENT_PASSWORD_INCORRECT,
                 PASSWORD_CONFIRMATION_MISMATCH,
                 NEW_PASSWORD_SAME_AS_CURRENT ->
                    HttpStatus.BAD_REQUEST;

            case ACTIVATION_ATTEMPTS_EXCEEDED,
                 ACTIVATION_RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;

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
        String traceId =
                resolveTraceId(request);

        ErrorResponse response =
                ErrorResponse.of(
                        errorCode.name(),
                        message,
                        fieldErrors,
                        request.getRequestURI(),
                        traceId
                );

        HttpHeaders headers = new HttpHeaders();
        headers.set(TRACE_ID_HEADER, traceId);

        return new ResponseEntity<>(
                response,
                headers,
                status
        );
    }

    private String resolveTraceId(
            HttpServletRequest request
    ) {
        String suppliedTraceId =
                request.getHeader(TRACE_ID_HEADER);

        if (suppliedTraceId == null
                || suppliedTraceId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return suppliedTraceId.trim();
    }
}
