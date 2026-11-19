package com.pasteleriamilsabores.pasteleria.api.dto;

import com.pasteleriamilsabores.pasteleria.api.model.Rol;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;

import java.time.LocalDate;

/**
 * Datos del usuario que se envían en los JSON. No incluye la contraseña.
 */
public record UsuarioDTO(
        String run,
        String nombre,
        String apellidos,
        String correo,
        LocalDate fechaNacimiento,
        String region,
        String comuna,
        String direccion,
        Rol rol
) {
    public static UsuarioDTO desde(Usuario u) {
        return new UsuarioDTO(u.getRun(), u.getNombre(), u.getApellidos(), u.getCorreo(),
                u.getFechaNacimiento(), u.getRegion(), u.getComuna(), u.getDireccion(), u.getRol());
    }
}