
package com.pasteleriamilsabores.pasteleria.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BoletaResponse(
        Long id,
        LocalDateTime fecha,
        Integer subtotal,
        Integer descuento,
        Integer total,
        List<Detalle> detalle
) {
    public record Detalle(
            String codigo,
            String nombre,
            Integer cantidad,
            Integer precioUnitario,
            Integer subtotal
    ) {
    }
}
