
package com.pasteleriamilsabores.pasteleria.api.service;

import com.pasteleriamilsabores.pasteleria.api.dto.CategoriaRequest;
import com.pasteleriamilsabores.pasteleria.api.exception.ConflictoException;
import com.pasteleriamilsabores.pasteleria.api.exception.RecursoNoEncontradoException;
import com.pasteleriamilsabores.pasteleria.api.model.Categoria;
import com.pasteleriamilsabores.pasteleria.api.repository.CategoriaRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria obtener(Long id) {
        return buscar(id);
    }

    @Transactional
    public Categoria crear(CategoriaRequest datos) {
        String slug = datos.slug().trim();
        String nombre = datos.nombre().trim();

        if (categoriaRepository.existsBySlug(slug)) {
            throw new ConflictoException("El slug ya está registrado.");
        }
        if (categoriaRepository.existsByNombre(nombre)) {
            throw new ConflictoException("El nombre de la categoría ya existe.");
        }

        return categoriaRepository.save(new Categoria(slug, nombre));
    }

    @Transactional
    public Categoria actualizar(Long id, CategoriaRequest datos) {
        Categoria categoria = buscar(id);
        String slug = datos.slug().trim();
        String nombre = datos.nombre().trim();

        categoriaRepository.findBySlug(slug).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new ConflictoException("El slug ya está registrado.");
            }
        });

        categoriaRepository.findByNombre(nombre).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new ConflictoException("El nombre de la categoría ya existe.");
            }
        });

        categoria.setSlug(slug);
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);

        if (!productoRepository.findByCategoriaSlugOrderByNombre(categoria.getSlug()).isEmpty()) {
            throw new ConflictoException("No se puede eliminar una categoría que tiene productos asociados.");
        }

        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada."));
    }
}
