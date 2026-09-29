package com.autoservice.bookingservice.exception;

public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(
            String message
    ) {
        super(message);
    }
}