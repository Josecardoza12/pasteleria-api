
package com.pasteleriamilsabores.pasteleria.api.controller;

import com.pasteleriamilsabores.pasteleria.api.dto.ProductoDTO;
import com.pasteleriamilsabores.pasteleria.api.dto.ProductoRequest;
import com.pasteleriamilsabores.pasteleria.api.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD de productos. Consultas públicas y modificaciones solo para ADMINISTRADOR.
 */
@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "CRUD y consultas de productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos",
            description = "Lista todos los productos o filtra por categoría.")
    public List<ProductoDTO> listar(@RequestParam(required = false) String categoria) {
        if (categoria != null && !categoria.isBlank()) {
            return productoService.listarPorCategoria(categoria);
        }
        return productoService.listar();
    }

    @GetMapping("/stock-critico")
    @Operation(summary = "Productos con stock crítico",
            description = "Lista productos cuyo stock es menor o igual al stock crítico.")
    public List<ProductoDTO> stockCritico() {
        return productoService.listarStockCritico();
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar por código",
            description = "Busca un producto usando su código único.")
    public ProductoDTO obtenerPorCodigo(@PathVariable String codigo) {
        return productoService.obtenerPorCodigo(codigo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver producto",
            description = "Busca un producto por su ID.")
    public ProductoDTO obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear producto",
            description = "Solo ADMINISTRADOR. Crea un producto.")
    public ProductoDTO crear(@Valid @RequestBody ProductoRequest datos) {
        return productoService.crear(datos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto",
            description = "Solo ADMINISTRADOR. Actualiza un producto existente.")
    public ProductoDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest datos) {
        return productoService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar producto",
            description = "Solo ADMINISTRADOR. Elimina un producto existente.")
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}
