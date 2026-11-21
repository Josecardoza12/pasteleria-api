 package com.pasteleriamilsabores.pasteleria.api.service;

import com.pasteleriamilsabores.pasteleria.api.dto.BoletaRequest;
import com.pasteleriamilsabores.pasteleria.api.dto.BoletaResponse;
import com.pasteleriamilsabores.pasteleria.api.dto.LineaBoleta;
import com.pasteleriamilsabores.pasteleria.api.model.Boleta;
import com.pasteleriamilsabores.pasteleria.api.model.DetalleBoleta;
import com.pasteleriamilsabores.pasteleria.api.model.Producto;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import com.pasteleriamilsabores.pasteleria.api.repository.BoletaRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.ProductoRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public BoletaService(
            BoletaRepository boletaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public BoletaResponse crear(BoletaRequest request) {

        if (request == null || request.lineas() == null
                || request.lineas().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La compra debe contener productos."
            );
        }

        String cupon = request.cupon() == null
                ? ""
                : request.cupon().trim();

        if (!cupon.isEmpty() && !cupon.equalsIgnoreCase("FELICES10")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El cupón ingresado no es válido."
            );
        }

        Authentication autenticacion =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || "anonymousUser".equals(autenticacion.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para comprar."
            );
        }

        String correo = autenticacion.getName();

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "No se encontró el usuario autenticado."
                ));

        Map<String, Integer> cantidades = new LinkedHashMap<>();

        for (LineaBoleta linea : request.lineas()) {

            if (linea == null || linea.codigo() == null
                    || linea.codigo().isBlank()
                    || linea.cantidad() == null
                    || linea.cantidad() < 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Los productos y cantidades deben ser válidos."
                );
            }

            String codigo = linea.codigo().trim();

            try {
                cantidades.merge(codigo, linea.cantidad(), Math::addExact);
            } catch (ArithmeticException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La cantidad solicitada es demasiado grande."
                );
            }
        }

        Boleta boleta = new Boleta();
        boleta.setUsuario(usuario);
        boleta.setCupon(cupon.isEmpty() ? null : cupon);

        List<DetalleBoleta> detalles = new ArrayList<>();
        int subtotal = 0;

        for (Map.Entry<String, Integer> linea : cantidades.entrySet()) {

            Producto producto = productoRepository
                    .findByCodigoForUpdate(linea.getKey())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No existe el producto: " + linea.getKey()
                    ));

            int cantidad = linea.getValue();

            if (producto.getStock() < cantidad) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No hay stock suficiente de «"
                                + producto.getNombre()
                                + "»: quedan "
                                + producto.getStock()
                                + " unidades."
                );
            }

            int precioUnitario = producto.getPrecio();
            int subtotalLinea;

            try {
                subtotalLinea = Math.multiplyExact(cantidad, precioUnitario);
                subtotal = Math.addExact(subtotal, subtotalLinea);
            } catch (ArithmeticException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El monto de la compra supera el límite permitido."
                );
            }

            producto.setStock(producto.getStock() - cantidad);

            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setBoleta(boleta);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotalLinea);

            detalles.add(detalle);
        }

        int descuento = cupon.equalsIgnoreCase("FELICES10")
                ? (int) (subtotal * 10L / 100)
                : 0;

        boleta.setSubtotal(subtotal);
        boleta.setDescuento(descuento);
        boleta.setTotal(subtotal - descuento);
        boleta.setDetalles(detalles);

        Boleta guardada = boletaRepository.save(boleta);

        List<BoletaResponse.Detalle> respuestaDetalles =
                guardada.getDetalles().stream()
                        .map(detalle -> new BoletaResponse.Detalle(
                                detalle.getProducto().getCodigo(),
                                detalle.getProducto().getNombre(),
                                detalle.getCantidad(),
                                detalle.getPrecioUnitario(),
                                detalle.getSubtotal()
                        ))
                        .toList();

        return new BoletaResponse(
                guardada.getId(),
                guardada.getFecha(),
                guardada.getSubtotal(),
                guardada.getDescuento(),
                guardada.getTotal(),
                respuestaDetalles
        );
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listarMias() {
        Authentication autenticacion = obtenerAutenticacion();

        return boletaRepository
                .findByUsuarioCorreoOrderByFechaDesc(
                        autenticacion.getName()
                )
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listarTodas() {
        return boletaRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoletaResponse buscarPorId(Long id) {
        Authentication autenticacion = obtenerAutenticacion();

        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe la boleta solicitada."
                ));

        boolean esPersonal = autenticacion.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol ->
                        "ROLE_ADMINISTRADOR".equals(rol)
                                || "ROLE_VENDEDOR".equals(rol)
                );

        boolean esDueno = Objects.equals(
                boleta.getUsuario().getCorreo(),
                autenticacion.getName()
        );

        if (!esPersonal && !esDueno) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para consultar esta boleta."
            );
        }

        return convertirRespuesta(boleta);
    }

    private Authentication obtenerAutenticacion() {
        Authentication autenticacion =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || "anonymousUser".equals(autenticacion.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión."
            );
        }

        return autenticacion;
    }

    private BoletaResponse convertirRespuesta(Boleta boleta) {
        List<BoletaResponse.Detalle> detalles = boleta
                .getDetalles()
                .stream()
                .map(detalle -> new BoletaResponse.Detalle(
                        detalle.getProducto().getCodigo(),
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ))
                .toList();

        return new BoletaResponse(
                boleta.getId(),
                boleta.getFecha(),
                boleta.getSubtotal(),
                boleta.getDescuento(),
                boleta.getTotal(),
                detalles
        );
    }

}
