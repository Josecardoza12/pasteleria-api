package com.pasteleriamilsabores.pasteleria.api.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/** Lo que envía React al registrarse. Siempre crea un usuario CLIENTE. */
public record RegistroRequest(
        @NotBlank(message = "El RUN es obligatorio.")
        @RunValido
        String run,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres.")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres.")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio.")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
        @Pattern(regexp = "^[^\\s@]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Ingrese un correo @duoc.cl, @profesor.duoc.cl o @gmail.com.")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres.")
        String contrasena,

        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura.")
        LocalDate fechaNacimiento,

        @NotBlank(message = "Debe seleccionar una región.")
        String region,

        @NotBlank(message = "Debe seleccionar una comuna.")
        String comuna,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 300, message = "La dirección no puede superar los 300 caracteres.")
        String direccion
) {
}