package com.pasteleriamilsabores.pasteleria.api.service;

import com.pasteleriamilsabores.pasteleria.api.dto.AuthResponse;
import com.pasteleriamilsabores.pasteleria.api.dto.LoginRequest;
import com.pasteleriamilsabores.pasteleria.api.dto.RefreshRequest;
import com.pasteleriamilsabores.pasteleria.api.dto.RegistroRequest;
import com.pasteleriamilsabores.pasteleria.api.exception.ConflictoException;
import com.pasteleriamilsabores.pasteleria.api.model.Rol;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import com.pasteleriamilsabores.pasteleria.api.repository.UsuarioRepository;
import com.pasteleriamilsabores.pasteleria.api.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * S3-11: pruebas de AuthService (login, registro y renovación).
 * El repositorio y el AuthenticationManager son simulados (Mockito);
 * BCrypt y JwtService son reales para comprobar el cifrado y los tokens.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRETO = "clave-solo-para-pruebas-con-mas-de-32-caracteres-1234";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService(SECRETO, 15, 7);

    private AuthService authService;
    private Usuario admin;

    @BeforeEach
    void preparar() {
        authService = new AuthService(authenticationManager, usuarioRepository, passwordEncoder, jwtService);
        admin = new Usuario("111111111", "Administrador", "Mil Sabores", "admin@gmail.com",
                passwordEncoder.encode("admin123"), null,
                "Región del Biobío", "Concepción", "Av. San Martín 1234", Rol.ADMINISTRADOR);
    }

    // Datos válidos de registro; cada prueba cambia lo que necesita
    private RegistroRequest registroValido() {
        return new RegistroRequest("19011022K", "Ana", "Pérez Soto", "ana@gmail.com", "ana1234",
                LocalDate.of(2000, 5, 14), "Región Metropolitana de Santiago", "Melipilla", "Av. Ortúzar 123");
    }

    @Test
    @DisplayName("Login correcto: entrega accessToken, refreshToken y el usuario sin contraseña")
    void loginCorrecto() {
        when(usuarioRepository.findByCorreo("admin@gmail.com")).thenReturn(Optional.of(admin));

        // El correo llega con espacios y mayúsculas: el servicio lo normaliza
        AuthResponse respuesta = authService.login(new LoginRequest("  ADMIN@gmail.com ", "admin123"));

        assertNotNull(respuesta.accessToken());
        assertNotNull(respuesta.refreshToken());
        assertEquals("Bearer", respuesta.tipo());
        assertEquals(900, respuesta.expiraEn());
        assertEquals(Rol.ADMINISTRADOR, respuesta.usuario().rol());
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("admin@gmail.com", "admin123"));
    }

    @Test
    @DisplayName("Login incorrecto: lanza BadCredentialsException (la API responde 401)")
    void loginIncorrecto() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("admin@gmail.com", "mala123")));
        // Si la contraseña no calza, ni siquiera se busca al usuario ni se generan tokens
        verify(usuarioRepository, never()).findByCorreo(any());
    }

    @Test
    @DisplayName("Registro: crea un CLIENTE con la contraseña cifrada con BCrypt")
    void registroGuardaClienteConContrasenaCifrada() {
        AuthResponse respuesta = authService.registrar(registroValido());

        // ArgumentCaptor atrapa el Usuario que el servicio le pasó a save()
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertEquals(Rol.CLIENTE, guardado.getRol());
        assertNotEquals("ana1234", guardado.getContrasena());              // nunca en texto plano
        assertTrue(guardado.getContrasena().startsWith("$2a$"));            // formato BCrypt
        assertTrue(passwordEncoder.matches("ana1234", guardado.getContrasena()));
        assertNotNull(respuesta.accessToken());                            // queda con la sesión iniciada
    }

    @Test
    @DisplayName("Registro con RUN repetido: lanza ConflictoException (409) y no guarda")
    void registroRunDuplicado() {
        when(usuarioRepository.existsByRun("19011022K")).thenReturn(true);

        ConflictoException error = assertThrows(ConflictoException.class,
                () -> authService.registrar(registroValido()));

        assertEquals("El RUN ya está registrado.", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro con correo repetido: lanza ConflictoException (409) y no guarda")
    void registroCorreoDuplicado() {
        when(usuarioRepository.existsByCorreo("ana@gmail.com")).thenReturn(true);

        ConflictoException error = assertThrows(ConflictoException.class,
                () -> authService.registrar(registroValido()));

        assertEquals("El correo ya está registrado.", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refresh con un refresh token válido entrega tokens nuevos")
    void refreshValido() {
        when(usuarioRepository.findByCorreo("admin@gmail.com")).thenReturn(Optional.of(admin));
        String refreshToken = jwtService.generarRefreshToken(admin);

        AuthResponse respuesta = authService.refrescar(new RefreshRequest(refreshToken));

        assertNotNull(respuesta.accessToken());
        assertEquals("admin@gmail.com", respuesta.usuario().correo());
    }

    @Test
    @DisplayName("Refresh usando un access token: lanza JwtException (la API responde 401)")
    void refreshConAccessToken() {
        String accessToken = jwtService.generarAccessToken(admin);

        assertThrows(JwtException.class, () -> authService.refrescar(new RefreshRequest(accessToken)));
    }
}