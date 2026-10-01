package com.autoservice.notificationservice.domain.enums;

public enum NotificationType {
    BOOKING_CREATED,
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    BOOKING_REMINDER,

    REPAIR_ORDER_CREATED,
    REPAIR_STATUS_CHANGED,
    REPAIR_COMPLETED,

    INVOICE_ISSUED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,

    GENERAL
}