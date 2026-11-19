package com.pasteleriamilsabores.pasteleria.api.exception;

/**
 * Error de validación de un campo que no se puede revisar con anotaciones
 * (por ejemplo, contraseña obligatoria solo al crear). Se responde 400 con ese campo marcado.
 */
public class CampoInvalidoException extends RuntimeException {

    private final String campo;

    public CampoInvalidoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}