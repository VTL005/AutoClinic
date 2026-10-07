package com.autoservice.vehicleservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class CurrentOrPastYearValidator
        implements ConstraintValidator<CurrentOrPastYear, Integer> {

    @Override
    public boolean isValid(
            Integer value,
            ConstraintValidatorContext context
    ) {
        // Giá trị null được kiểm tra riêng bằng @NotNull.
        if (value == null) {
            return true;
        }

        int currentYear = Year.now(context.getClockProvider().getClock())
                .getValue();

        return value <= currentYear;
    }
}
