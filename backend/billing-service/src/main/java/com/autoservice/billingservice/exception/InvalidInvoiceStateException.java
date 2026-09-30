package com.autoservice.billingservice.exception;

public class InvalidInvoiceStateException
        extends RuntimeException {

    public InvalidInvoiceStateException(
            String message
    ) {
        super(message);
    }
}