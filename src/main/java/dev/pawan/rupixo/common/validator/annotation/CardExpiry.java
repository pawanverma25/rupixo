package dev.pawan.rupixo.common.validator.annotation;

import dev.pawan.rupixo.common.validator.CardExpiryValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CardExpiryValidator.class)
@Documented
public @interface CardExpiry {
    String message() default "Card is already expired.";

    int expiryYear();
    int expiryMonth();

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
