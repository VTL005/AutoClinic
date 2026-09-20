package com.autoservice.vehicleservice.exception;

public class ResourceNotFoundException
        extends BusinessException {

    public ResourceNotFoundException(
            ErrorCode errorCode,
            String message
    ) {
        super(errorCode, message);
    }
}