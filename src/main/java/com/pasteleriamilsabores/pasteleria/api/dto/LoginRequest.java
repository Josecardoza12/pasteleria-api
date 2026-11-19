package com.pasteleriamilsabores.pasteleria.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Lo que envía React al iniciar sesión. */
public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio.")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria.")
        String contrasena
) {
}