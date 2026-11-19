package com.pasteleriamilsabores.pasteleria.api.dto;

/**
 * Respuesta de login, registro y refresh.
 * expiraEn: segundos que dura el accessToken (900 = 15 minutos).
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tipo,
        long expiraEn,
        UsuarioDTO usuario
) {
}