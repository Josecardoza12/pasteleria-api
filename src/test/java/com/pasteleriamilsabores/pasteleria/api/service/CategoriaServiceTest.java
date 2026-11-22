
package com.pasteleriamilsabores.pasteleria.api.service;

import com.pasteleriamilsabores.pasteleria.api.dto.CategoriaRequest;
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
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;
    private CategoriaRequest datosValidos;

    @BeforeEach
    void prepararDatos() {
        categoria = new Categoria(
                "cuadradas",
                "Tortas Cuadradas"
        );

        datosValidos = new CategoriaRequest(
                "especiales",
                "Tortas Especiales"
        );
    }

    // C3-10: listar categorías existentes.
    @Test
    void debeListarTodasLasCategorias() {
        when(categoriaRepository.findAll())
                .thenReturn(List.of(categoria));

        List<Categoria> resultado = categoriaService.listar();

        assertEquals(1, resultado.size());
        assertEquals("cuadradas", resultado.get(0).getSlug());
        assertEquals(
                "Tortas Cuadradas",
                resultado.get(0).getNombre()
        );

        verify(categoriaRepository).findAll();
    }

    // C3-10: obtener una categoría por ID.
    @Test
    void debeObtenerCategoriaExistente() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        Categoria resultado = categoriaService.obtener(1L);

        assertNotNull(resultado);
        assertEquals("cuadradas", resultado.getSlug());
        assertEquals(
                "Tortas Cuadradas",
                resultado.getNombre()
        );

        verify(categoriaRepository).findById(1L);
    }

    // C3-10: categoría inexistente.
    @Test
    void debeRechazarCategoriaInexistente() {
        when(categoriaRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> categoriaService.obtener(999L)
        );

        verify(categoriaRepository).findById(999L);
    }

    // C3-10: crear una categoría válida.
    @Test
    void debeCrearCategoriaValida() {
        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0)
                );

        Categoria resultado = categoriaService.crear(datosValidos);

        assertNotNull(resultado);
        assertEquals("especiales", resultado.getSlug());
        assertEquals(
                "Tortas Especiales",
                resultado.getNombre()
        );

        verify(categoriaRepository)
                .existsBySlug("especiales");

        verify(categoriaRepository)
                .existsByNombre("Tortas Especiales");

        verify(categoriaRepository)
                .save(any(Categoria.class));
    }

    // C3-10: eliminar espacios antes de guardar.
    @Test
    void debeEliminarEspaciosAlCrearCategoria() {
        CategoriaRequest datos = new CategoriaRequest(
                "  especiales  ",
                "  Tortas Especiales  "
        );

        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0)
                );

        Categoria resultado = categoriaService.crear(datos);

        assertEquals("especiales", resultado.getSlug());
        assertEquals(
                "Tortas Especiales",
                resultado.getNombre()
        );

        verify(categoriaRepository)
                .existsBySlug("especiales");

        verify(categoriaRepository)
                .existsByNombre("Tortas Especiales");
    }

    // C3-10: impedir identificadores duplicados.
    @Test
    void debeRechazarSlugDuplicado() {
        when(categoriaRepository.existsBySlug("especiales"))
                .thenReturn(true);

        assertThrows(
                ConflictoException.class,
                () -> categoriaService.crear(datosValidos)
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    // C3-10: impedir nombres duplicados.
    @Test
    void debeRechazarNombreDuplicado() {
        when(categoriaRepository.existsByNombre("Tortas Especiales"))
                .thenReturn(true);

        assertThrows(
                ConflictoException.class,
                () -> categoriaService.crear(datosValidos)
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    // C3-10: actualizar una categoría existente.
    @Test
    void debeActualizarCategoriaExistente() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0)
                );

        Categoria resultado = categoriaService.actualizar(
                1L,
                datosValidos
        );

        assertEquals("especiales", resultado.getSlug());
        assertEquals(
                "Tortas Especiales",
                resultado.getNombre()
        );

        verify(categoriaRepository).save(categoria);
    }

    // C3-10: impedir cambiar a un slug
    // utilizado por otra categoría.
    @Test
    void debeRechazarSlugDuplicadoAlActualizar() {
        Categoria categoriaExistente = mock(Categoria.class);

        when(categoriaExistente.getId())
                .thenReturn(2L);

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository.findBySlug("especiales"))
                .thenReturn(Optional.of(categoriaExistente));

        assertThrows(
                ConflictoException.class,
                () -> categoriaService.actualizar(
                        1L,
                        datosValidos
                )
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    // C3-10: impedir cambiar a un nombre
    // utilizado por otra categoría.
    @Test
    void debeRechazarNombreDuplicadoAlActualizar() {
        Categoria categoriaExistente = mock(Categoria.class);

        when(categoriaExistente.getId())
                .thenReturn(2L);

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository.findByNombre("Tortas Especiales"))
                .thenReturn(Optional.of(categoriaExistente));

        assertThrows(
                ConflictoException.class,
                () -> categoriaService.actualizar(
                        1L,
                        datosValidos
                )
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    // C3-10: eliminar una categoría sin productos.
    @Test
    void debeEliminarCategoriaSinProductos() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(
                productoRepository
                        .findByCategoriaSlugOrderByNombre("cuadradas")
        ).thenReturn(List.of());

        categoriaService.eliminar(1L);

        verify(productoRepository)
                .findByCategoriaSlugOrderByNombre("cuadradas");

        verify(categoriaRepository)
                .delete(categoria);
    }

    // C3-10: impedir eliminar una categoría
    // que tiene productos asociados.
    @Test
    void debeRechazarEliminarCategoriaConProductos() {
        Producto producto = new Producto(
                "TC001",
                "Torta de Chocolate",
                "Torta artesanal",
                45000,
                5,
                3,
                "/img/torta.jpg",
                categoria
        );

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(
                productoRepository
                        .findByCategoriaSlugOrderByNombre("cuadradas")
        ).thenReturn(List.of(producto));

        assertThrows(
                ConflictoException.class,
                () -> categoriaService.eliminar(1L)
        );

        verify(categoriaRepository, never())
                .delete(any(Categoria.class));
    }

    // C3-10: impedir eliminar una categoría
    // que no existe.
    @Test
    void debeRechazarEliminarCategoriaInexistente() {
        when(categoriaRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> categoriaService.eliminar(999L)
        );

        verify(categoriaRepository, never())
                .delete(any(Categoria.class));
    }
}
