
package com.pasteleriamilsabores.pasteleria.api.repository;
import com.pasteleriamilsabores.pasteleria.api.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findBySlug(String slug);

    Optional<Categoria> findByNombre(String nombre);

    boolean existsBySlug(String slug);

    boolean existsByNombre(String nombre);
}
