
package com.pasteleriamilsabores.pasteleria.api.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio.")
        @Size(min = 3, message = "El código debe tener al menos 3 caracteres.")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        String descripcion,

        @NotNull(message = "El precio es obligatorio.")
        @Min(value = 1, message = "El precio debe ser mayor a cero.")
        Integer precio,

        @NotNull(message = "El stock es obligatorio.")
        @Min(value = 0, message = "El stock no puede ser negativo.")
        Integer stock,

        @NotNull(message = "El stock crítico es obligatorio.")
        @Min(value = 0, message = "El stock crítico no puede ser negativo.")
        Integer stockCritico,

        String imagen,

        @NotBlank(message = "La categoría es obligatoria.")
        String categoria
) {
}
