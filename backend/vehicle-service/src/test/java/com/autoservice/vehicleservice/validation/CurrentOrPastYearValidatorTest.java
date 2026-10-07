package com.autoservice.vehicleservice.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentOrPastYearValidatorTest {

    private record YearInput(@CurrentOrPastYear Integer year) {
    }

    @Test
    void acceptsPastAndCurrentYearButRejectsNextYear() {
        try (ValidatorFactory factory = factoryAt("2026-10-03T23:00:00Z")) {
            Validator validator = factory.getValidator();
            assertTrue(validator.validate(new YearInput(2025)).isEmpty());
            assertTrue(validator.validate(new YearInput(2026)).isEmpty());
            assertFalse(validator.validate(new YearInput(2027)).isEmpty());
        }
    }

    @Test
    void usesClockZoneWhenYearChanges() {
        // UTC vẫn là năm 2026, nhưng tại Việt Nam đã sang năm 2027.
        try (ValidatorFactory factory = factoryAt("2026-12-31T17:00:00Z")) {
            Validator validator = factory.getValidator();
            assertTrue(validator.validate(new YearInput(2027)).isEmpty());
            assertFalse(validator.validate(new YearInput(2028)).isEmpty());
        }
    }

    @Test
    void leavesNullValidationToNotNullConstraint() {
        try (ValidatorFactory factory = factoryAt("2026-10-03T23:00:00Z")) {
            assertTrue(factory.getValidator()
                    .validate(new YearInput(null)).isEmpty());
        }
    }

    private ValidatorFactory factoryAt(String instant) {
        Clock clock = Clock.fixed(
                Instant.parse(instant), ZoneId.of("Asia/Ho_Chi_Minh"));

        return Validation.byDefaultProvider().configure()
                .clockProvider(() -> clock)
                .buildValidatorFactory();
    }
}
