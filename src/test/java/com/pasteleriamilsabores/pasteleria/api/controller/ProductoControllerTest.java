
package com.pasteleriamilsabores.pasteleria.api.controller;

import com.pasteleriamilsabores.pasteleria.api.dto.ProductoDTO;
import com.pasteleriamilsabores.pasteleria.api.dto.ProductoRequest;
import com.pasteleriamilsabores.pasteleria.api.exception.ConflictoException;
import com.pasteleriamilsabores.pasteleria.api.exception.ManejadorErrores;
import com.pasteleriamilsabores.pasteleria.api.exception.RecursoNoEncontradoException;
import com.pasteleriamilsabores.pasteleria.api.service.ProductoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ManejadorErrores.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    // Producto utilizado para las respuestas simuladas.
    private ProductoDTO productoEjemplo() {
        return new ProductoDTO(
                1L,
                "TC001",
                "Torta de Chocolate",
                "Torta artesanal de chocolate",
                45000,
                5,
                3,
                "/img/torta-chocolate.jpg",
                "cuadradas",
                "Tortas Cuadradas"
        );
    }

    // JSON válido para las peticiones POST y PUT.
    private String productoValidoJson() {
        return """
                {
                    "codigo": "TC002",
                    "nombre": "Torta de Vainilla",
                    "descripcion": "Torta artesanal",
                    "precio": 25000,
                    "stock": 10,
                    "stockCritico": 3,
                    "imagen": "/img/torta-vainilla.jpg",
                    "categoria": "cuadradas"
                }
                """;
    }

    // C3-10: GET público para listar productos.
    @Test
    void debeListarProductosConEstado200() throws Exception {
        when(productoService.listar())
                .thenReturn(List.of(productoEjemplo()));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo")
                        .value("TC001"))
                .andExpect(jsonPath("$[0].nombre")
                        .value("Torta de Chocolate"))
                .andExpect(jsonPath("$[0].precio")
                        .value(45000))
                .andExpect(jsonPath("$[0].categoria")
                        .value("cuadradas"));

        verify(productoService).listar();
    }

    // C3-10: filtrar productos por categoría.
    @Test
    void debeFiltrarProductosPorCategoria() throws Exception {
        when(productoService.listarPorCategoria("cuadradas"))
                .thenReturn(List.of(productoEjemplo()));

        mockMvc.perform(
                        get("/api/v1/productos")
                                .param("categoria", "cuadradas")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo")
                        .value("TC001"));

        verify(productoService)
                .listarPorCategoria("cuadradas");
    }

    // C3-10: consultar stock crítico.
    @Test
    void debeConsultarStockCritico() throws Exception {
        ProductoDTO productoCritico = new ProductoDTO(
                2L,
                "TC003",
                "Torta Especial",
                "Producto con stock bajo",
                30000,
                2,
                5,
                "/img/torta-especial.jpg",
                "cuadradas",
                "Tortas Cuadradas"
        );

        when(productoService.listarStockCritico())
                .thenReturn(List.of(productoCritico));

        mockMvc.perform(
                        get("/api/v1/productos/stock-critico")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo")
                        .value("TC003"))
                .andExpect(jsonPath("$[0].stock")
                        .value(2))
                .andExpect(jsonPath("$[0].stockCritico")
                        .value(5));

        verify(productoService)
                .listarStockCritico();
    }

    // C3-10: buscar un producto mediante su código.
    @Test
    void debeObtenerProductoPorCodigo() throws Exception {
        when(productoService.obtenerPorCodigo("TC001"))
                .thenReturn(productoEjemplo());

        mockMvc.perform(
                        get("/api/v1/productos/codigo/TC001")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo")
                        .value("TC001"))
                .andExpect(jsonPath("$.nombre")
                        .value("Torta de Chocolate"));

        verify(productoService)
                .obtenerPorCodigo("TC001");
    }

    // C3-10: crear un producto válido.
    @Test
    void debeCrearProductoValidoConEstado201() throws Exception {
        when(productoService.crear(any(ProductoRequest.class)))
                .thenReturn(productoEjemplo());

        mockMvc.perform(
                        post("/api/v1/productos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productoValidoJson())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo")
                        .value("TC001"))
                .andExpect(jsonPath("$.precio")
                        .value(45000));

        verify(productoService)
                .crear(any(ProductoRequest.class));
    }

    // C3-10: @Min(1) impide guardar precio cero.
    @Test
    void debeRechazarPrecioCeroConEstado400() throws Exception {
        String json = """
                {
                    "codigo": "TC002",
                    "nombre": "Torta de Vainilla",
                    "descripcion": "Torta artesanal",
                    "precio": 0,
                    "stock": 10,
                    "stockCritico": 3,
                    "imagen": "/img/torta-vainilla.jpg",
                    "categoria": "cuadradas"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/productos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.campos.precio")
                        .value("El precio debe ser mayor a cero."));

        verify(productoService, never())
                .crear(any(ProductoRequest.class));
    }

    // C3-10: @Min(0) impide guardar stock negativo.
    @Test
    void debeRechazarStockNegativoConEstado400() throws Exception {
        String json = """
                {
                    "codigo": "TC002",
                    "nombre": "Torta de Vainilla",
                    "descripcion": "Torta artesanal",
                    "precio": 25000,
                    "stock": -1,
                    "stockCritico": 3,
                    "imagen": "/img/torta-vainilla.jpg",
                    "categoria": "cuadradas"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/productos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.campos.stock")
                        .value("El stock no puede ser negativo."));

        verify(productoService, never())
                .crear(any(ProductoRequest.class));
    }

    // C3-10: código duplicado devuelve HTTP 409.
    @Test
    void debeRechazarCodigoDuplicadoConEstado409() throws Exception {
        when(productoService.crear(any(ProductoRequest.class)))
                .thenThrow(
                        new ConflictoException(
                                "El código del producto ya está registrado."
                        )
                );

        mockMvc.perform(
                        post("/api/v1/productos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productoValidoJson())
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.mensaje")
                        .value(
                                "El código del producto ya está registrado."
                        ));

        verify(productoService)
                .crear(any(ProductoRequest.class));
    }

    // C3-10: producto inexistente devuelve HTTP 404.
    @Test
    void debeResponder404SiProductoNoExiste() throws Exception {
        when(productoService.obtener(999L))
                .thenThrow(
                        new RecursoNoEncontradoException(
                                "Producto no encontrado."
                        )
                );

        mockMvc.perform(
                        get("/api/v1/productos/999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.mensaje")
                        .value("Producto no encontrado."));

        verify(productoService).obtener(999L);
    }

    // C3-10: eliminar un producto devuelve HTTP 204.
    @Test
    void debeEliminarProductoConEstado204() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/productos/1")
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productoService).eliminar(1L);
    }
}
