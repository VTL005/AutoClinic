package com.autoservice.vehicleservice.util;

import java.util.Locale;

public final class LicensePlateUtils {

    private LicensePlateUtils() {
        throw new IllegalStateException(
                "Utility class must not be instantiated"
        );
    }

    public static String normalize(String licensePlate) {
        if (licensePlate == null
                || licensePlate.isBlank()) {
            return null;
        }

        return licensePlate
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
    }
}