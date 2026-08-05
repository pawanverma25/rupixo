package dev.pawan.rupixo.common.validator;

import dev.pawan.rupixo.common.validator.annotation.CardExpiry;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CardExpiryValidator implements ConstraintValidator<CardExpiry, Object> {

    @Override
    public void initialize(CardExpiry constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return false;
    }
}
