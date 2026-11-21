
package com.pasteleriamilsabores.pasteleria.api.service;

import com.pasteleriamilsabores.pasteleria.api.dto.BoletaRequest;
import com.pasteleriamilsabores.pasteleria.api.dto.BoletaResponse;
import com.pasteleriamilsabores.pasteleria.api.dto.LineaBoleta;
import com.pasteleriamilsabores.pasteleria.api.model.Boleta;
import com.pasteleriamilsabores.pasteleria.api.model.Producto;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import com.pasteleriamilsabores.pasteleria.api.repository.BoletaRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.ProductoRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.UsuarioRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private BoletaService boletaService;

    @BeforeEach
    void configurar() {
        var autenticacion = new UsernamePasswordAuthenticationToken(
                "cliente@gmail.com",
                null,
                List.of()
        );

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacion);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void configurarUsuario() {
        Usuario usuario = mock(Usuario.class);

        when(usuarioRepository.findByCorreo("cliente@gmail.com"))
                .thenReturn(Optional.of(usuario));
    }

    private Producto crearProducto(int stock, int precio) {
        Producto producto = new Producto();
        producto.setCodigo("TC001");
        producto.setNombre("Torta de Chocolate");
        producto.setPrecio(precio);
        producto.setStock(stock);
        return producto;
    }

    private void configurarGuardado() {
        when(boletaRepository.save(any(Boleta.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void compraExitosaDescuentaStock() {
        configurarUsuario();

        Producto producto = crearProducto(10, 15000);

        when(productoRepository.findByCodigoForUpdate("TC001"))
                .thenReturn(Optional.of(producto));

        configurarGuardado();

        BoletaRequest request = new BoletaRequest(
                List.of(new LineaBoleta("TC001", 2)),
                null
        );

        BoletaResponse respuesta = boletaService.crear(request);

        assertEquals(8, producto.getStock());
        assertEquals(30000, respuesta.subtotal());
        assertEquals(0, respuesta.descuento());
        assertEquals(30000, respuesta.total());
        assertEquals(1, respuesta.detalle().size());

        verify(boletaRepository).save(any(Boleta.class));
    }

    @Test
    void stockInsuficienteRechazaCompra() {
        configurarUsuario();

        Producto producto = crearProducto(1, 15000);

        when(productoRepository.findByCodigoForUpdate("TC001"))
                .thenReturn(Optional.of(producto));

        BoletaRequest request = new BoletaRequest(
                List.of(new LineaBoleta("TC001", 3)),
                null
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> boletaService.crear(request)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(1, producto.getStock());

        verify(boletaRepository, never()).save(any());
    }

    @Test
    void productoInexistenteDevuelve404() {
        configurarUsuario();

        when(productoRepository.findByCodigoForUpdate("TC999"))
                .thenReturn(Optional.empty());

        BoletaRequest request = new BoletaRequest(
                List.of(new LineaBoleta("TC999", 1)),
                null
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> boletaService.crear(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        verify(boletaRepository, never()).save(any());
    }

    @Test
    void cuponFelices10AplicaDescuento() {
        configurarUsuario();

        Producto producto = crearProducto(10, 20000);

        when(productoRepository.findByCodigoForUpdate("TC001"))
                .thenReturn(Optional.of(producto));

        configurarGuardado();

        BoletaRequest request = new BoletaRequest(
                List.of(new LineaBoleta("TC001", 2)),
                "FELICES10"
        );

        BoletaResponse respuesta = boletaService.crear(request);

        assertEquals(40000, respuesta.subtotal());
        assertEquals(4000, respuesta.descuento());
        assertEquals(36000, respuesta.total());
        assertEquals(8, producto.getStock());
    }

    @Test
    void cuponInvalidoDevuelve400() {
        BoletaRequest request = new BoletaRequest(
                List.of(new LineaBoleta("TC001", 1)),
                "CUPONINVALIDO"
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> boletaService.crear(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());

        verify(boletaRepository, never()).save(any());
        verifyNoInteractions(productoRepository);
    }
}
