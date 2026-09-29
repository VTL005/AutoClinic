package com.autoservice.repairservice.exception;

public class InvalidRepairStateException
        extends RuntimeException {

    public InvalidRepairStateException(
            String message
    ) {
        super(message);
    }
}