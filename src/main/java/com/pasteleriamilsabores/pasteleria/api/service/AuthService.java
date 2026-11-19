package com.pasteleriamilsabores.pasteleria.api.service;


import com.pasteleriamilsabores.pasteleria.api.dto.*;
import com.pasteleriamilsabores.pasteleria.api.exception.ConflictoException;
import com.pasteleriamilsabores.pasteleria.api.exception.RecursoNoEncontradoException;
import com.pasteleriamilsabores.pasteleria.api.model.Rol;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import com.pasteleriamilsabores.pasteleria.api.repository.UsuarioRepository;
import com.pasteleriamilsabores.pasteleria.api.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de autenticación: login, registro, renovación de tokens y usuario actual.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Comprueba correo y contraseña (BCrypt). Si no calzan, Spring lanza BadCredentialsException (401). */
    public AuthResponse login(LoginRequest datos) {
        String correo = datos.correo().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(correo, datos.contrasena()));

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return crearRespuesta(usuario);
    }

    /** Registro público: siempre crea un CLIENTE y lo deja con la sesión iniciada. */
    @Transactional
    public AuthResponse registrar(RegistroRequest datos) {
        String run = datos.run().trim().toUpperCase();
        String correo = datos.correo().trim().toLowerCase();

        if (usuarioRepository.existsByRun(run)) {
            throw new ConflictoException("El RUN ya está registrado.");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("El correo ya está registrado.");
        }

        Usuario usuario = new Usuario(run, datos.nombre().trim(), datos.apellidos().trim(), correo,
                passwordEncoder.encode(datos.contrasena()), datos.fechaNacimiento(),
                datos.region(), datos.comuna(), datos.direccion().trim(), Rol.CLIENTE);
        usuarioRepository.save(usuario);

        return crearRespuesta(usuario);
    }

    /** Entrega tokens nuevos si el refresh token es válido. Si no, JwtException (401). */
    public AuthResponse refrescar(RefreshRequest datos) {
        Claims claims = jwtService.validar(datos.refreshToken(), JwtService.TIPO_REFRESH);

        Usuario usuario = usuarioRepository.findByCorreo(claims.getSubject())
                .orElseThrow(() -> new JwtException("El usuario del token ya no existe"));
        return crearRespuesta(usuario);
    }

    /** Datos del usuario dueño del token (GET /auth/me). */
    public UsuarioDTO usuarioActual(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return UsuarioDTO.desde(usuario);
    }

    private AuthResponse crearRespuesta(Usuario usuario) {
        return new AuthResponse(
                jwtService.generarAccessToken(usuario),
                jwtService.generarRefreshToken(usuario),
                "Bearer",
                jwtService.getSegundosAccess(),
                UsuarioDTO.desde(usuario));
    }
}