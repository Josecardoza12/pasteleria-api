package com.pasteleriamilsabores.pasteleria.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Lo que envía React para renovar los tokens. */
public record RefreshRequest(
        @NotBlank(message = "El refreshToken es obligatorio.")
        String refreshToken
) {
}