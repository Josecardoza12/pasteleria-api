package com.pasteleriamilsabores.pasteleria.api.dto;

import com.pasteleriamilsabores.pasteleria.api.service.ValidadorRun;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Lógica de @RunValido: usa el mismo módulo 11 de ValidadorRun. */
public class RunValidoValidator implements ConstraintValidator<RunValido, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null || valor.isBlank()) {
            return true; // el vacío lo reporta @NotBlank
        }
        return ValidadorRun.esValido(valor);
    }
}