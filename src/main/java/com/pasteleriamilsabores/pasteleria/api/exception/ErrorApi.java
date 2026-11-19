package com.pasteleriamilsabores.pasteleria.api.exception;

import java.util.Map;

/**
 * Forma única de todos los errores de la API.
 * Ejemplo: { "status": 404, "mensaje": "Producto no encontrado", "campos": {} }
 */
public record ErrorApi(int status, String mensaje, Map<String, String> campos) {

    public static ErrorApi de(int status, String mensaje) {
        return new ErrorApi(status, mensaje, Map.of());
    }
}