
package com.pasteleriamilsabores.pasteleria.api.controller;

import com.pasteleriamilsabores.pasteleria.api.dto.CategoriaRequest;
import com.pasteleriamilsabores.pasteleria.api.model.Categoria;
import com.pasteleriamilsabores.pasteleria.api.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD de categorías. Consultas públicas y modificaciones solo para ADMINISTRADOR.
 */
@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías", description = "CRUD de categorías de productos")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías",
            description = "Lista todas las categorías disponibles.")
    public List<Categoria> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver una categoría",
            description = "Busca una categoría por ID. 404 si no existe.")
    public Categoria obtener(@PathVariable Long id) {
        return categoriaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear categoría",
            description = "Solo ADMINISTRADOR. 201 si se creó, 400 si hay datos inválidos y 409 si está duplicada.")
    public Categoria crear(@Valid @RequestBody CategoriaRequest datos) {
        return categoriaService.crear(datos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar categoría",
            description = "Solo ADMINISTRADOR. 404 si no existe y 409 si el slug o nombre está duplicado.")
    public Categoria actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest datos) {
        return categoriaService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar categoría",
            description = "Solo ADMINISTRADOR. 204 si se eliminó, 404 si no existe y 409 si tiene productos asociados.")
    public void eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
    }
}
