package com.pasteleriamilsabores.pasteleria.api.controller;

import com.pasteleriamilsabores.pasteleria.api.dto.*;
import com.pasteleriamilsabores.pasteleria.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login, registro y renovación de tokens JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements   // ruta pública: sin candado en Swagger
    @Operation(summary = "Iniciar sesión",
            description = "Recibe correo y contraseña. Responde 200 con accessToken (15 min), "
                    + "refreshToken (7 días) y los datos del usuario. 401 si las credenciales no son correctas.")
    public AuthResponse login(@Valid @RequestBody LoginRequest datos) {
        return authService.login(datos);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(summary = "Registrarse como cliente",
            description = "Crea un usuario con rol CLIENTE y responde 201 con sus tokens. "
                    + "400 si hay datos inválidos, 409 si el RUN o el correo ya existen.")
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest datos) {
        return authService.registrar(datos);
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Renovar tokens",
            description = "Recibe el refreshToken y entrega tokens nuevos. 401 si venció o no es un refresh token.")
    public AuthResponse refrescar(@Valid @RequestBody RefreshRequest datos) {
        return authService.refrescar(datos);
    }

    @GetMapping("/me")
    @Operation(summary = "Usuario actual",
            description = "Devuelve los datos del usuario dueño del token. 401 sin token o con token inválido.")
    public UsuarioDTO me(Authentication autenticacion) {
        return authService.usuarioActual(autenticacion.getName());
    }
}