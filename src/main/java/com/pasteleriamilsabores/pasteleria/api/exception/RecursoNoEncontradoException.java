package com.pasteleriamilsabores.pasteleria.api.exception;

/** Se lanza cuando un recurso no existe. Se responde con 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}