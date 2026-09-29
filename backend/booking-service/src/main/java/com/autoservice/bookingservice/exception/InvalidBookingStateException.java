package com.autoservice.bookingservice.exception;

public class InvalidBookingStateException
        extends RuntimeException {

    public InvalidBookingStateException(
            String message
    ) {
        super(message);
    }
}