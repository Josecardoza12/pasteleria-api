
package com.pasteleriamilsabores.pasteleria.api.controller;

import com.pasteleriamilsabores.pasteleria.api.dto.BoletaRequest;
import com.pasteleriamilsabores.pasteleria.api.dto.BoletaResponse;
import com.pasteleriamilsabores.pasteleria.api.service.BoletaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "Gestión de compras y órdenes")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Registrar una compra")
    public ResponseEntity<BoletaResponse> crear(
            @Valid @RequestBody BoletaRequest request
    ) {
        BoletaResponse respuesta = boletaService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Consultar mis compras")
    public List<BoletaResponse> misCompras() {
        return boletaService.listarMias();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Listar todas las órdenes")
    public List<BoletaResponse> listar() {
        return boletaService.listarTodas();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar una boleta por su ID")
    public BoletaResponse buscarPorId(@PathVariable Long id) {
        return boletaService.buscarPorId(id);
    }
}
