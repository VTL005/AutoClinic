package com.autoservice.notificationservice.exception;

public class DuplicateResourceException
        extends BusinessException {

    public DuplicateResourceException(
            ErrorCode errorCode,
            String message
    ) {
        super(errorCode, message);
    }
}