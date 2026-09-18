package com.autoservice.identityservice.exception;

public class DuplicateResourceException
        extends BusinessException {

    public DuplicateResourceException(
            ErrorCode errorCode,
            String message
    ) {
        super(errorCode, message);
    }
}