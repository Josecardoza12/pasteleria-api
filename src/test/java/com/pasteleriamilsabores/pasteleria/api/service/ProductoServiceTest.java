
package com.pasteleriamilsabores.pasteleria.api.service;

import com.pasteleriamilsabores.pasteleria.api.dto.ProductoDTO;
import com.pasteleriamilsabores.pasteleria.api.dto.ProductoRequest;
import com.pasteleriamilsabores.pasteleria.api.exception.ConflictoException;
import com.pasteleriamilsabores.pasteleria.api.exception.RecursoNoEncontradoException;
import com.pasteleriamilsabores.pasteleria.api.model.Categoria;
import com.pasteleriamilsabores.pasteleria.api.model.Producto;
import com.pasteleriamilsabores.pasteleria.api.repository.CategoriaRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;
    private Producto producto;
    private ProductoRequest datosValidos;

    @BeforeEach
    void prepararDatos() {

        categoria = new Categoria(
                "cuadradas",
                "Tortas Cuadradas"
        );

        producto = new Producto(
                "TC001",
                "Torta de Chocolate",
                "Torta de chocolate artesanal",
                45000,
                5,
                3,
                "/img/torta-chocolate.jpg",
                categoria
        );

        datosValidos = new ProductoRequest(
                "TC002",
                "Torta de Vainilla",
                "Torta de vainilla artesanal",
                25000,
                10,
                3,
                "/img/torta-vainilla.jpg",
                "cuadradas"
        );
    }

    // C3-10: listar todos los productos.
    @Test
    void debeListarTodosLosProductos() {

        when(productoRepository.findAll())
                .thenReturn(List.of(producto));

        List<ProductoDTO> resultado = productoService.listar();

        assertEquals(1, resultado.size());
        assertEquals("TC001", resultado.get(0).codigo());

        verify(productoRepository).findAll();
    }

    // C3-10: obtener un producto mediante su ID.
    @Test
    void debeObtenerUnProductoExistente() {

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        ProductoDTO resultado = productoService.obtener(1L);

        assertNotNull(resultado);
        assertEquals("TC001", resultado.codigo());
        assertEquals("Torta de Chocolate", resultado.nombre());

        verify(productoRepository).findById(1L);
    }

    // C3-10: producto inexistente.
    @Test
    void debeRechazarProductoInexistente() {

        when(productoRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> productoService.obtener(999L)
        );

        verify(productoRepository).findById(999L);
    }

    // C3-10: crear un producto válido.
    @Test
    void debeCrearProductoValido() {

        when(productoRepository.existsByCodigo("TC002"))
                .thenReturn(false);

        when(categoriaRepository.findBySlug("cuadradas"))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0)
                );

        ProductoDTO resultado = productoService.crear(datosValidos);

        assertNotNull(resultado);
        assertEquals("TC002", resultado.codigo());
        assertEquals("Torta de Vainilla", resultado.nombre());
        assertEquals(25000, resultado.precio());
        assertEquals(10, resultado.stock());

        verify(productoRepository)
                .existsByCodigo("TC002");

        verify(categoriaRepository)
                .findBySlug("cuadradas");

        verify(productoRepository)
                .save(any(Producto.class));
    }

    // C3-10: impedir códigos duplicados.
    @Test
    void debeRechazarCodigoDuplicado() {

        when(productoRepository.existsByCodigo("TC002"))
                .thenReturn(true);

        assertThrows(
                ConflictoException.class,
                () -> productoService.crear(datosValidos)
        );

        verify(productoRepository)
                .existsByCodigo("TC002");

        verify(productoRepository, never())
                .save(any(Producto.class));
    }

    // C3-10: impedir crear productos con
    // categorías inexistentes.
    @Test
    void debeRechazarCategoriaInexistente() {

        when(productoRepository.existsByCodigo("TC002"))
                .thenReturn(false);

        when(categoriaRepository.findBySlug("cuadradas"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> productoService.crear(datosValidos)
        );

        verify(productoRepository, never())
                .save(any(Producto.class));
    }

    // C3-10: filtrar productos por categoría.
    @Test
    void debeListarProductosPorCategoria() {

        when(
                productoRepository
                        .findByCategoriaSlugOrderByNombre("cuadradas")
        ).thenReturn(List.of(producto));

        List<ProductoDTO> resultado =
                productoService.listarPorCategoria("cuadradas");

        assertEquals(1, resultado.size());
        assertEquals("TC001", resultado.get(0).codigo());

        verify(productoRepository)
                .findByCategoriaSlugOrderByNombre("cuadradas");
    }

    // C3-10: consultar los productos con stock crítico.
    @Test
    void debeListarProductosConStockCritico() {

        Producto productoCritico = new Producto(
                "TC003",
                "Torta Especial",
                "Producto con stock bajo",
                30000,
                2,
                5,
                "/img/torta-especial.jpg",
                categoria
        );

        when(productoRepository.findStockCritico())
                .thenReturn(List.of(productoCritico));

        List<ProductoDTO> resultado =
                productoService.listarStockCritico();

        assertEquals(1, resultado.size());
        assertEquals("TC003", resultado.get(0).codigo());
        assertEquals(2, resultado.get(0).stock());

        verify(productoRepository).findStockCritico();
    }

    // C3-10: actualizar un producto existente.
    @Test
    void debeActualizarProductoExistente() {

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(productoRepository.findByCodigo("TC002"))
                .thenReturn(Optional.empty());

        when(categoriaRepository.findBySlug("cuadradas"))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0)
                );

        ProductoDTO resultado =
                productoService.actualizar(1L, datosValidos);

        assertEquals("TC002", resultado.codigo());
        assertEquals("Torta de Vainilla", resultado.nombre());
        assertEquals(25000, resultado.precio());

        verify(productoRepository)
                .save(producto);
    }

    // C3-10: eliminar un producto existente.
    @Test
    void debeEliminarProductoExistente() {

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        productoService.eliminar(1L);

        verify(productoRepository)
                .findById(1L);

        verify(productoRepository)
                .delete(producto);
    }
}
