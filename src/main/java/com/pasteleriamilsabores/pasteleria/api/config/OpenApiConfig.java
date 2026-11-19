package com.pasteleriamilsabores.pasteleria.api.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger: título de la API y botón Authorize para el JWT.
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * Convención: todos los controladores usan @RequestMapping("/api/v1/...").
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Pastelería Mil Sabores",
                version = "v1",
                description = "API REST de la Pastelería Mil Sabores: autenticación, usuarios, "
                        + "categorías, productos y boletas. Todas las rutas están bajo /api/v1."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}