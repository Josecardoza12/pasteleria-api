package com.pasteleriamilsabores.pasteleria.api.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** Valida que el campo sea un RUN chileno válido (sin puntos ni guion, con dígito verificador). */
@Documented
@Constraint(validatedBy = RunValidoValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunValido {

    String message() default "Ingrese un RUN válido, sin puntos ni guion.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}