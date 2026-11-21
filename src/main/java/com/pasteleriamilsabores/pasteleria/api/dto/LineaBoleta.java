
package com.pasteleriamilsabores.pasteleria.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LineaBoleta(
        @NotBlank String codigo,
        @NotNull @Min(1) Integer cantidad
) {
}
