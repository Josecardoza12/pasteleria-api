package com.pasteleriamilsabores.pasteleria.api.exception;


/** Se lanza cuando la operación choca con el estado actual de los datos. Se responde con 409. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}