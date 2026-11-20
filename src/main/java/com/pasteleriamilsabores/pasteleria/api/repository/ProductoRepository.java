
package com.pasteleriamilsabores.pasteleria.api.repository;
import com.pasteleriamilsabores.pasteleria.api.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Producto> findByCategoriaSlugOrderByNombre(String slug);

    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockCritico")
    List<Producto> findStockCritico();
}
