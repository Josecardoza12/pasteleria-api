package com.pasteleriamilsabores.pasteleria.api.controller;

import com.pasteleriamilsabores.pasteleria.api.dto.UsuarioDTO;
import com.pasteleriamilsabores.pasteleria.api.dto.UsuarioRequest;
import com.pasteleriamilsabores.pasteleria.api.model.Rol;
import com.pasteleriamilsabores.pasteleria.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD de usuarios. Todas las rutas son solo para ADMINISTRADOR (regla en SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "CRUD de usuarios (solo ADMINISTRADOR)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios",
            description = "Lista todos los usuarios ordenados por nombre. Filtro opcional ?rol=ADMINISTRADOR, VENDEDOR o CLIENTE.")
    public List<UsuarioDTO> listar(@RequestParam(required = false) Rol rol) {
        return usuarioService.listar(rol);
    }

    @GetMapping("/{run}")
    @Operation(summary = "Ver un usuario", description = "Busca por RUN. 404 si no existe.")
    public UsuarioDTO obtener(@PathVariable String run) {
        return usuarioService.obtener(run);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario",
            description = "Crea un usuario con cualquier rol. 201 si se creó, 400 si hay datos inválidos, "
                    + "409 si el RUN o el correo ya existen.")
    public UsuarioDTO crear(@Valid @RequestBody UsuarioRequest datos) {
        return usuarioService.crear(datos);
    }

    @PutMapping("/{run}")
    @Operation(summary = "Editar usuario",
            description = "El RUN no cambia. Si la contraseña viene vacía se mantiene la actual. "
                    + "404 si no existe, 409 si el correo pertenece a otro usuario.")
    public UsuarioDTO actualizar(@PathVariable String run, @Valid @RequestBody UsuarioRequest datos) {
        return usuarioService.actualizar(run, datos);
    }

    @DeleteMapping("/{run}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar usuario",
            description = "204 si se eliminó, 404 si no existe, 409 si intentas eliminar tu propia cuenta "
                    + "o si el usuario tiene boletas.")
    public void eliminar(@PathVariable String run, Authentication autenticacion) {
        usuarioService.eliminar(run, autenticacion.getName());
    }
}