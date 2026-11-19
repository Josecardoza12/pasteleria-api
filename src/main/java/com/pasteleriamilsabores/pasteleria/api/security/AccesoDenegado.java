package com.pasteleriamilsabores.pasteleria.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Se ejecuta cuando hay sesión, pero el rol no tiene permiso para esa ruta
 * (por ejemplo, un VENDEDOR que intenta crear un producto).
 * Responde 403 con la misma forma de ErrorApi.
 */
@Component
public class AccesoDenegado implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":403,\"mensaje\":\"No tienes permiso para realizar esta acción\",\"campos\":{}}");
    }
}