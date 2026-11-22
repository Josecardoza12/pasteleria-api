package com.pasteleriamilsabores.pasteleria.api.security;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * S3-11: pruebas de seguridad de punta a punta.
 * Levanta la API completa (Spring Security + filtro JWT + controladores + H2 con DatosIniciales)
 * y hace peticiones HTTP con MockMvc, igual que React o Swagger.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    // Inicia sesión con una cuenta de DatosIniciales y devuelve su accessToken
    private String tokenDe(String correo, String contrasena) throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.accessToken");
    }

    // ---------- Rutas públicas ----------

    @Test
    @DisplayName("Swagger y el catálogo de productos se abren sin token")
    void rutasPublicas() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
    }

    // ---------- Login (401 y 200) ----------

    @Test
    @DisplayName("Login correcto: 200 con tokens y el usuario, sin la contraseña")
    void loginCorrecto() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@gmail.com\",\"contrasena\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.usuario.contrasena").doesNotExist());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta: 401 con formato ErrorApi")
    void loginIncorrecto() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@gmail.com\",\"contrasena\":\"mala123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    // ---------- Sin token o token inválido (401) ----------

    @Test
    @DisplayName("Sin token, una ruta protegida responde 401")
    void sinToken() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Con un token falso, una ruta protegida responde 401")
    void tokenFalso() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer esto.no.es-un-token"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Roles (200 y 403) ----------

    @Test
    @DisplayName("El administrador lista los usuarios (200)")
    void administradorListaUsuarios() throws Exception {
        String token = tokenDe("admin@gmail.com", "admin123");

        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].contrasena").doesNotExist());
    }

    @Test
    @DisplayName("El vendedor no puede ver los usuarios (403)")
    void vendedorNoVeUsuarios() throws Exception {
        String token = tokenDe("vendedor@gmail.com", "vend123");

        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("El vendedor sí puede ver las órdenes y el stock crítico (200)")
    void vendedorVeOrdenesYStockCritico() throws Exception {
        String token = tokenDe("vendedor@gmail.com", "vend123");

        mockMvc.perform(get("/api/v1/boletas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/productos/stock-critico").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("El cliente no puede ver el listado de órdenes (403)")
    void clienteNoVeOrdenes() throws Exception {
        String token = tokenDe("cliente@gmail.com", "cliente123");

        mockMvc.perform(get("/api/v1/boletas").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Con token, /auth/me devuelve el usuario dueño del token")
    void usuarioActual() throws Exception {
        String token = tokenDe("cliente@gmail.com", "cliente123");

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("cliente@gmail.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    // ---------- Validación (400) ----------

    @Test
    @DisplayName("Registro con RUN y correo inválidos: 400 con un error por campo")
    void registroConDatosInvalidos() throws Exception {
        String json = """
                {"run":"123","nombre":"Ana","apellidos":"Pérez","correo":"ana@hotmail.com",
                 "contrasena":"ana1234","region":"Región Metropolitana de Santiago",
                 "comuna":"Melipilla","direccion":"Av. Ortúzar 123"}
                """;

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.run").exists())
                .andExpect(jsonPath("$.campos.correo").exists());
    }
}