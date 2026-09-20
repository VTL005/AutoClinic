package com.autoservice.vehicleservice.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class VinUtils {

    public static final int VIN_LENGTH = 17;

    private static final Pattern VALID_VIN_PATTERN =
            Pattern.compile(
                    "^[A-HJ-NPR-Z0-9]{17}$"
            );

    private VinUtils() {
        throw new IllegalStateException(
                "Utility class must not be instantiated"
        );
    }

    public static String normalize(String vin) {
        if (vin == null) {
            return null;
        }

        return vin
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    public static boolean isValid(String vin) {
        String normalizedVin = normalize(vin);

        return normalizedVin != null
                && VALID_VIN_PATTERN
                .matcher(normalizedVin)
                .matches();
    }

    public static boolean containsForbiddenCharacters(
            String vin
    ) {
        String normalizedVin = normalize(vin);

        if (normalizedVin == null) {
            return false;
        }

        return normalizedVin.contains("I")
                || normalizedVin.contains("O")
                || normalizedVin.contains("Q");
    }
}