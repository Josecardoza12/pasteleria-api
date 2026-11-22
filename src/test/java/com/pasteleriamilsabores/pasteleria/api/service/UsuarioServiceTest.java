package com.pasteleriamilsabores.pasteleria.api.service;

import com.pasteleriamilsabores.pasteleria.api.dto.UsuarioDTO;
import com.pasteleriamilsabores.pasteleria.api.dto.UsuarioRequest;
import com.pasteleriamilsabores.pasteleria.api.exception.CampoInvalidoException;
import com.pasteleriamilsabores.pasteleria.api.exception.ConflictoException;
import com.pasteleriamilsabores.pasteleria.api.exception.RecursoNoEncontradoException;
import com.pasteleriamilsabores.pasteleria.api.model.Rol;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import com.pasteleriamilsabores.pasteleria.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * S3-11: pruebas de UsuarioService (CRUD de usuarios del administrador).
 * El repositorio es simulado (Mockito); BCrypt es real.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UsuarioService usuarioService;

    @BeforeEach
    void preparar() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    // Usuario ya guardado en la "base de datos"
    private Usuario pedro() {
        return new Usuario("123456785", "Pedro", "González Rojas", "pedro@duoc.cl", "hash-original", null,
                "Región Metropolitana de Santiago", "Melipilla", "Calle Serrano 456", Rol.VENDEDOR);
    }

    // Datos que envía el administrador; contrasena puede ser vacía
    private UsuarioRequest datos(String nombre, String correo, String contrasena) {
        return new UsuarioRequest("123456785", nombre, "González Rojas", correo, contrasena, null,
                "Región Metropolitana de Santiago", "Melipilla", "Calle Serrano 456", Rol.VENDEDOR);
    }

    @Test
    @DisplayName("Crear: guarda el usuario con el rol elegido y la contraseña cifrada")
    void crearUsuarioValido() {
        // save() devuelve el mismo usuario que recibe, como lo haría la base de datos
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        UsuarioDTO creado = usuarioService.crear(datos("Pedro", "pedro@duoc.cl", "pedro123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(Rol.VENDEDOR, creado.rol());
        assertTrue(passwordEncoder.matches("pedro123", captor.getValue().getContrasena()));
    }

    @Test
    @DisplayName("Crear sin contraseña: error 400 marcado en el campo contrasena")
    void crearSinContrasena() {
        CampoInvalidoException error = assertThrows(CampoInvalidoException.class,
                () -> usuarioService.crear(datos("Pedro", "pedro@duoc.cl", "")));

        assertEquals("contrasena", error.getCampo());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear con RUN repetido: error 409 y no guarda")
    void crearRunRepetido() {
        when(usuarioRepository.existsByRun("123456785")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> usuarioService.crear(datos("Pedro", "pedro@duoc.cl", "pedro123")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Listar con filtro de rol: consulta solo ese rol")
    void listarPorRol() {
        when(usuarioRepository.findByRolOrderByNombreAsc(Rol.VENDEDOR)).thenReturn(List.of(pedro()));

        List<UsuarioDTO> lista = usuarioService.listar(Rol.VENDEDOR);

        assertEquals(1, lista.size());
        assertEquals("Pedro", lista.get(0).nombre());
        verify(usuarioRepository, never()).findAllByOrderByNombreAsc();
    }

    @Test
    @DisplayName("Obtener un RUN que no existe: error 404")
    void obtenerInexistente() {
        assertThrows(RecursoNoEncontradoException.class, () -> usuarioService.obtener("999999999"));
    }

    @Test
    @DisplayName("Editar con contraseña vacía: cambia los datos y mantiene la contraseña actual")
    void editarSinCambiarContrasena() {
        Usuario existente = pedro();
        when(usuarioRepository.findByRun("123456785")).thenReturn(Optional.of(existente));

        UsuarioDTO editado = usuarioService.actualizar("123456785", datos("Pedro Pablo", "pedro@duoc.cl", ""));

        assertEquals("Pedro Pablo", editado.nombre());
        assertEquals("123456785", editado.run());                 // el RUN no cambia
        assertEquals("hash-original", existente.getContrasena());  // la contraseña se mantiene
    }

    @Test
    @DisplayName("Editar usando el correo de otro usuario: error 409")
    void editarConCorreoDeOtro() {
        Usuario otro = mock(Usuario.class);
        when(otro.getId()).thenReturn(2L);
        when(usuarioRepository.findByRun("123456785")).thenReturn(Optional.of(pedro()));
        when(usuarioRepository.findByCorreo("admin@gmail.com")).thenReturn(Optional.of(otro));

        assertThrows(ConflictoException.class,
                () -> usuarioService.actualizar("123456785", datos("Pedro", "admin@gmail.com", "")));
    }

    @Test
    @DisplayName("El administrador no puede eliminar su propia cuenta: error 409")
    void noEliminarseASiMismo() {
        Usuario admin = new Usuario("111111111", "Administrador", "Mil Sabores", "admin@gmail.com", "hash", null,
                "Región del Biobío", "Concepción", "Av. San Martín 1234", Rol.ADMINISTRADOR);
        when(usuarioRepository.findByRun("111111111")).thenReturn(Optional.of(admin));

        ConflictoException error = assertThrows(ConflictoException.class,
                () -> usuarioService.eliminar("111111111", "admin@gmail.com"));

        assertEquals("No puedes eliminar tu propia cuenta.", error.getMessage());
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Eliminar a otro usuario: se borra del repositorio")
    void eliminarOtroUsuario() {
        Usuario existente = pedro();
        when(usuarioRepository.findByRun("123456785")).thenReturn(Optional.of(existente));

        usuarioService.eliminar("123456785", "admin@gmail.com");

        verify(usuarioRepository).delete(existente);
    }
}