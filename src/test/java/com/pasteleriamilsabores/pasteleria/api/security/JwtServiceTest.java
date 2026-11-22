package com.pasteleriamilsabores.pasteleria.api.security;

import com.pasteleriamilsabores.pasteleria.api.model.Rol;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S3-11: pruebas de JwtService (generación y verificación de tokens).
 * No levanta Spring: se crea el servicio con new y una clave de prueba.
 */
class JwtServiceTest {

    private static final String SECRETO = "clave-solo-para-pruebas-con-mas-de-32-caracteres-1234";

    private JwtService jwtService;
    private Usuario admin;
    private Usuario cliente;

    @BeforeEach
    void preparar() {
        // 15 minutos el access token y 7 días el refresh token, igual que en application.properties
        jwtService = new JwtService(SECRETO, 15, 7);
        admin = new Usuario("111111111", "Administrador", "Mil Sabores", "admin@gmail.com", "hash", null,
                "Región del Biobío", "Concepción", "Av. San Martín 1234", Rol.ADMINISTRADOR);
        cliente = new Usuario("333333333", "Cliente", "Mil Sabores", "cliente@gmail.com", "hash", null,
                "Región Metropolitana de Santiago", "Santiago", "Av. Libertador 1000", Rol.CLIENTE);
    }

    @Test
    @DisplayName("El access token generado trae el correo y el rol del usuario")
    void accessTokenTraeCorreoYRol() {
        String token = jwtService.generarAccessToken(admin);

        Claims claims = jwtService.validar(token, JwtService.TIPO_ACCESS);

        assertEquals("admin@gmail.com", claims.getSubject());
        assertEquals("ADMINISTRADOR", claims.get("rol", String.class));
        assertEquals("access", claims.get("tipo", String.class));
    }

    @Test
    @DisplayName("El access token dura 15 minutos (expiraEn = 900 segundos)")
    void accessTokenDura15Minutos() {
        assertEquals(900, jwtService.getSegundosAccess());
    }

    @Test
    @DisplayName("Un token vencido no es válido")
    void tokenVencidoNoEsValido() {
        // Un servicio con -1 minuto genera tokens que ya nacen vencidos
        JwtService servicioVencido = new JwtService(SECRETO, -1, 7);
        String tokenVencido = servicioVencido.generarAccessToken(admin);

        assertThrows(JwtException.class, () -> jwtService.validar(tokenVencido, JwtService.TIPO_ACCESS));
    }

    @Test
    @DisplayName("Un token alterado no es válido (la firma no calza)")
    void tokenAlteradoNoEsValido() {
        // Un cliente intenta hacerse pasar por administrador: toma su propio token
        // y le pega el contenido (payload) del token del admin, sin poder volver a firmarlo
        String[] tokenCliente = jwtService.generarAccessToken(cliente).split("\\.");
        String[] tokenAdmin = jwtService.generarAccessToken(admin).split("\\.");
        String tokenAlterado = tokenCliente[0] + "." + tokenAdmin[1] + "." + tokenCliente[2];

        assertThrows(JwtException.class, () -> jwtService.validar(tokenAlterado, JwtService.TIPO_ACCESS));
    }

    @Test
    @DisplayName("Un token firmado con otra clave no es válido")
    void tokenDeOtraClaveNoEsValido() {
        JwtService otraClave = new JwtService("otra-clave-distinta-tambien-de-mas-de-32-caracteres", 15, 7);
        String tokenAjeno = otraClave.generarAccessToken(admin);

        assertThrows(JwtException.class, () -> jwtService.validar(tokenAjeno, JwtService.TIPO_ACCESS));
    }

    @Test
    @DisplayName("Un access token no sirve como refresh token")
    void accessNoSirveComoRefresh() {
        String access = jwtService.generarAccessToken(admin);

        assertThrows(JwtException.class, () -> jwtService.validar(access, JwtService.TIPO_REFRESH));
    }

    @Test
    @DisplayName("Un refresh token no sirve como access token")
    void refreshNoSirveComoAccess() {
        String refresh = jwtService.generarRefreshToken(admin);

        assertThrows(JwtException.class, () -> jwtService.validar(refresh, JwtService.TIPO_ACCESS));
    }
}