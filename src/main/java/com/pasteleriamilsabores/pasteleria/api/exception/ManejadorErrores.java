package com.pasteleriamilsabores.pasteleria.api.exception;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte las excepciones de toda la API en respuestas ErrorApi.
 * Así React siempre recibe { status, mensaje, campos }.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    // 404: el recurso pedido no existe
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorApi> noEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 409: duplicados, stock insuficiente, categoría con productos, etc.
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorApi> conflicto(ConflictoException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 400: falló @Valid; se devuelve un mensaje por cada campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ErrorApi(400, "Revisa los datos marcados", campos));
    }

    // 400: tipo incorrecto en la ruta, por ejemplo /categorias/abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorApi> tipoIncorrecto(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST,
                "El valor '" + ex.getValue() + "' no es válido para '" + ex.getName() + "'");
    }

    // 400: el cuerpo no es un JSON válido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApi> jsonInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido");
    }

    // 400: reglas de negocio simples (RUN inválido, cupón inválido, etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorApi> argumentoInvalido(IllegalArgumentException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 401: login con correo o contraseña incorrectos
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorApi> credencialesIncorrectas(BadCredentialsException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
    }

    // 401: refresh token vencido, alterado o de otro tipo
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorApi> tokenInvalido(JwtException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "La sesión expiró o el token no es válido");
    }

    // 401 y 403 lanzados dentro de los controladores (por ejemplo con @PreAuthorize)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorApi> noAutenticado(AuthenticationException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorApi> accesoDenegado(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción");
    }

    // Cualquier otro error. Las excepciones propias de Spring (ruta inexistente,
    // método no permitido, etc.) conservan su código; el resto es 500 sin stack trace.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> otro(Exception ex) {
        if (ex instanceof ErrorResponse respuestaSpring) {
            HttpStatus status = HttpStatus.valueOf(respuestaSpring.getStatusCode().value());
            return responder(status, status == HttpStatus.NOT_FOUND
                    ? "La ruta solicitada no existe"
                    : status.getReasonPhrase());
        }
        log.error("Error no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor");
    }

    private ResponseEntity<ErrorApi> responder(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(ErrorApi.de(status.value(), mensaje));
    }
}