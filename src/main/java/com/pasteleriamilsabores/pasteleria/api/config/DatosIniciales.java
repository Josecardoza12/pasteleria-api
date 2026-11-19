package com.pasteleriamilsabores.pasteleria.api.config;

import com.pasteleriamilsabores.pasteleria.api.model.Rol;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import com.pasteleriamilsabores.pasteleria.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga los datos de prueba al arrancar la API, solo si las tablas están vacías.
 * Un método por módulo: cada integrante agrega el suyo.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        cargarUsuarios();
        // cargarCategorias();  -> Catalina (C3-01)
        // cargarProductos();   -> Catalina (C3-02)
    }

    // ---------- Usuarios (José, S3-05) ----------
    private void cargarUsuarios() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.saveAll(List.of(
                new Usuario("111111111", "Administrador", "Mil Sabores", "admin@gmail.com",
                        passwordEncoder.encode("admin123"), null,
                        "Región del Biobío", "Concepción", "Av. San Martín 1234", Rol.ADMINISTRADOR),
                new Usuario("222222222", "Vendedor", "Mil Sabores", "vendedor@gmail.com",
                        passwordEncoder.encode("vend123"), null,
                        "Región del Biobío", "Concepción", "Av. San Martín 1234", Rol.VENDEDOR),
                new Usuario("333333333", "Cliente", "Mil Sabores", "cliente@gmail.com",
                        passwordEncoder.encode("cliente123"), null,
                        "Región Metropolitana de Santiago", "Santiago", "Av. Libertador Bernardo O'Higgins 1000",
                        Rol.CLIENTE)
        ));
        log.info("Usuarios iniciales cargados: 3 (admin, vendedor y cliente)");
    }
}