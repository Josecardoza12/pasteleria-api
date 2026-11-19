package com.pasteleriamilsabores.pasteleria.api.repository;

import com.pasteleriamilsabores.pasteleria.api.model.Rol;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla usuario. Spring Data genera el SQL a partir del nombre de cada método.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);      // login

    Optional<Usuario> findByRun(String run);            // /usuarios/{run}

    boolean existsByCorreo(String correo);              // 409 si se repite

    boolean existsByRun(String run);                    // 409 si se repite

    List<Usuario> findByRolOrderByNombreAsc(Rol rol);   // /usuarios?rol=VENDEDOR

    List<Usuario> findAllByOrderByNombreAsc();          // /usuarios
}