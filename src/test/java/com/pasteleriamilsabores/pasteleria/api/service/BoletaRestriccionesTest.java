package com.pasteleriamilsabores.pasteleria.api.service;

import com.pasteleriamilsabores.pasteleria.api.model.Boleta;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import com.pasteleriamilsabores.pasteleria.api.repository.BoletaRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.ProductoRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoletaRestriccionesTest {

    @Mock BoletaRepository boletaRepository;
    @Mock ProductoRepository productoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks BoletaService boletaService;

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    private void iniciarSesion(String correo, String rol) {
        var autenticacion = new UsernamePasswordAuthenticationToken(
                correo, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }

    private Boleta boletaDe(String correo) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        Boleta boleta = new Boleta();
        boleta.setId(12L);
        boleta.setUsuario(usuario);
        boleta.setSubtotal(15000);
        boleta.setDescuento(0);
        boleta.setTotal(15000);
        boleta.setDetalles(List.of());
        return boleta;
    }

    @Test
    void clientePuedeConsultarSuPropiaBoleta() {
        iniciarSesion("cliente@ejemplo.cl", "CLIENTE");
        when(boletaRepository.findById(12L))
                .thenReturn(Optional.of(boletaDe("cliente@ejemplo.cl")));

        var respuesta = boletaService.buscarPorId(12L);

        assertEquals(12L, respuesta.id());
        assertEquals(15000, respuesta.total());
    }

    @Test
    void clienteNoPuedeConsultarBoletaAjena() {
        iniciarSesion("cliente@ejemplo.cl", "CLIENTE");
        when(boletaRepository.findById(12L))
                .thenReturn(Optional.of(boletaDe("otra@ejemplo.cl")));

        var error = assertThrows(ResponseStatusException.class,
                () -> boletaService.buscarPorId(12L));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void administradorPuedeConsultarBoletaDeCualquierCliente() {
        iniciarSesion("admin@ejemplo.cl", "ADMINISTRADOR");
        when(boletaRepository.findById(12L))
                .thenReturn(Optional.of(boletaDe("cliente@ejemplo.cl")));

        assertEquals(12L, boletaService.buscarPorId(12L).id());
    }

    @Test
    void vendedorPuedeConsultarBoletaDeCualquierCliente() {
        iniciarSesion("vendedor@ejemplo.cl", "VENDEDOR");
        when(boletaRepository.findById(12L))
                .thenReturn(Optional.of(boletaDe("cliente@ejemplo.cl")));

        assertEquals(12L, boletaService.buscarPorId(12L).id());
    }

    @Test
    void sinSesionNoPuedeConsultarBoleta() {
        var error = assertThrows(ResponseStatusException.class,
                () -> boletaService.buscarPorId(12L));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(boletaRepository);
    }

    @Test
    void boletaInexistenteDevuelve404() {
        iniciarSesion("cliente@ejemplo.cl", "CLIENTE");
        when(boletaRepository.findById(99L)).thenReturn(Optional.empty());

        var error = assertThrows(ResponseStatusException.class,
                () -> boletaService.buscarPorId(99L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void misComprasConsultaSoloElCorreoAutenticado() {
        iniciarSesion("cliente@ejemplo.cl", "CLIENTE");
        when(boletaRepository.findByUsuarioCorreoOrderByFechaDesc("cliente@ejemplo.cl"))
                .thenReturn(List.of(boletaDe("cliente@ejemplo.cl")));

        var compras = boletaService.listarMias();

        assertEquals(1, compras.size());
        assertEquals(12L, compras.get(0).id());
        verify(boletaRepository).findByUsuarioCorreoOrderByFechaDesc("cliente@ejemplo.cl");
        verify(boletaRepository, never()).findAllByOrderByFechaDesc();
    }
}
