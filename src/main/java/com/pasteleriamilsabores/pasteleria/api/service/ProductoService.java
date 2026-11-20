
package com.pasteleriamilsabores.pasteleria.api.service;

import com.pasteleriamilsabores.pasteleria.api.dto.ProductoDTO;
import com.pasteleriamilsabores.pasteleria.api.dto.ProductoRequest;
import com.pasteleriamilsabores.pasteleria.api.exception.ConflictoException;
import com.pasteleriamilsabores.pasteleria.api.exception.RecursoNoEncontradoException;
import com.pasteleriamilsabores.pasteleria.api.model.Categoria;
import com.pasteleriamilsabores.pasteleria.api.model.Producto;
import com.pasteleriamilsabores.pasteleria.api.repository.CategoriaRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listar() {
        return productoRepository.findAll().stream()
                .map(ProductoDTO::desdeEntidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtener(Long id) {
        return ProductoDTO.desdeEntidad(buscar(id));
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorCodigo(String codigo) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));
        return ProductoDTO.desdeEntidad(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorCategoria(String slug) {
        return productoRepository.findByCategoriaSlugOrderByNombre(slug).stream()
                .map(ProductoDTO::desdeEntidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarStockCritico() {
        return productoRepository.findStockCritico().stream()
                .map(ProductoDTO::desdeEntidad)
                .toList();
    }

    @Transactional
    public ProductoDTO crear(ProductoRequest datos) {
        String codigo = datos.codigo().trim();

        if (productoRepository.existsByCodigo(codigo)) {
            throw new ConflictoException("El código del producto ya está registrado.");
        }

        Categoria categoria = buscarCategoria(datos.categoria());

        Producto producto = new Producto(
                codigo,
                datos.nombre().trim(),
                datos.descripcion(),
                datos.precio(),
                datos.stock(),
                datos.stockCritico(),
                datos.imagen(),
                categoria
        );

        return ProductoDTO.desdeEntidad(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDTO actualizar(Long id, ProductoRequest datos) {
        Producto producto = buscar(id);
        String codigo = datos.codigo().trim();

        productoRepository.findByCodigo(codigo).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new ConflictoException("El código del producto ya está registrado.");
            }
        });

        Categoria categoria = buscarCategoria(datos.categoria());

        producto.setCodigo(codigo);
        producto.setNombre(datos.nombre().trim());
        producto.setDescripcion(datos.descripcion());
        producto.setPrecio(datos.precio());
        producto.setStock(datos.stock());
        producto.setStockCritico(datos.stockCritico());
        producto.setImagen(datos.imagen());
        producto.setCategoria(categoria);

        return ProductoDTO.desdeEntidad(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        productoRepository.delete(producto);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));
    }

    private Categoria buscarCategoria(String slug) {
        return categoriaRepository.findBySlug(slug.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada."));
    }
}
