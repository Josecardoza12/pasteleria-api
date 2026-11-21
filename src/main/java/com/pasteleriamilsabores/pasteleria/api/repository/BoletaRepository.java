
package com.pasteleriamilsabores.pasteleria.api.repository;

import com.pasteleriamilsabores.pasteleria.api.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findAllByOrderByFechaDesc();

    List<Boleta> findByUsuarioCorreoOrderByFechaDesc(String correo);
}
