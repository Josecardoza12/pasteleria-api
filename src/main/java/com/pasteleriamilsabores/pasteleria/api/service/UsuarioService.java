package com.pasteleriamilsabores.pasteleria.api.service;

import com.pasteleriamilsabores.pasteleria.api.dto.UsuarioDTO;
import com.pasteleriamilsabores.pasteleria.api.dto.UsuarioRequest;
import com.pasteleriamilsabores.pasteleria.api.exception.CampoInvalidoException;
import com.pasteleriamilsabores.pasteleria.api.exception.ConflictoException;
import com.pasteleriamilsabores.pasteleria.api.exception.RecursoNoEncontradoException;
import com.pasteleriamilsabores.pasteleria.api.model.Rol;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import com.pasteleriamilsabores.pasteleria.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica del CRUD de usuarios (solo para el administrador).
 */
@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Lista todos, o solo los de un rol si viene el filtro. */
    public List<UsuarioDTO> listar(Rol rol) {
        List<Usuario> usuarios = (rol == null)
                ? usuarioRepository.findAllByOrderByNombreAsc()
                : usuarioRepository.findByRolOrderByNombreAsc(rol);
        return usuarios.stream().map(UsuarioDTO::desde).toList();
    }

    public UsuarioDTO obtener(String run) {
        return UsuarioDTO.desde(buscar(run));
    }

    @Transactional
    public UsuarioDTO crear(UsuarioRequest datos) {
        if (datos.contrasena() == null || datos.contrasena().isBlank()) {
            throw new CampoInvalidoException("contrasena", "La contraseña es obligatoria.");
        }
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
                datos.region(), datos.comuna(), datos.direccion().trim(), datos.rol());
        return UsuarioDTO.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioDTO actualizar(String run, UsuarioRequest datos) {
        Usuario usuario = buscar(run);
        String correo = datos.correo().trim().toLowerCase();

        // El correo puede quedar igual, pero no puede ser el de OTRO usuario
        usuarioRepository.findByCorreo(correo)
                .filter(otro -> !otro.getId().equals(usuario.getId()))
                .ifPresent(otro -> {
                    throw new ConflictoException("El correo ya está registrado.");
                });

        // El RUN no se cambia
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setRegion(datos.region());
        usuario.setComuna(datos.comuna());
        usuario.setDireccion(datos.direccion().trim());
        usuario.setRol(datos.rol());

        // Contraseña vacía = se mantiene la actual
        if (datos.contrasena() != null && !datos.contrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(datos.contrasena()));
        }
        // No hace falta save(): JPA guarda los cambios al terminar la transacción
        return UsuarioDTO.desde(usuario);
    }

    /** correoActual = el administrador que hace la petición (no puede eliminarse a sí mismo). */
    @Transactional
    public void eliminar(String run, String correoActual) {
        Usuario usuario = buscar(run);
        if (usuario.getCorreo().equalsIgnoreCase(correoActual)) {
            throw new ConflictoException("No puedes eliminar tu propia cuenta.");
        }
        usuarioRepository.delete(usuario);
    }

    private Usuario buscar(String run) {
        return usuarioRepository.findByRun(run.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }
}