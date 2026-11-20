
package com.pasteleriamilsabores.pasteleria.api.dto;
import com.pasteleriamilsabores.pasteleria.api.model.Producto;

public record ProductoDTO(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Integer precio,
        Integer stock,
        Integer stockCritico,
        String imagen,
        String categoria,
        String categoriaNombre
) {
    public static ProductoDTO desdeEntidad(Producto producto) {
        return new ProductoDTO(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getStockCritico(),
                producto.getImagen(),
                producto.getCategoria().getSlug(),
                producto.getCategoria().getNombre()
        );
    }
}
