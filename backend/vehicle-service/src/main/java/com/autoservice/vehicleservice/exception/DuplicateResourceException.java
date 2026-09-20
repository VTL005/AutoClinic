package com.autoservice.vehicleservice.exception;

public class DuplicateResourceException
        extends BusinessException {

    public DuplicateResourceException(
            ErrorCode errorCode,
            String message
    ) {
        super(errorCode, message);
    }
}