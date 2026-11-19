package com.pasteleriamilsabores.pasteleria.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Se ejecuta una vez en cada petición:
 * 1. Lee el encabezado "Authorization: Bearer <token>".
 * 2. Si el token es un access token válido, carga al usuario y lo deja autenticado.
 * 3. Si no hay token o no es válido, sigue sin autenticar: si la ruta es protegida,
 *    EntradaNoAutorizada responde 401.
 * No lleva @Component: lo crea SecurityConfig, para que no se registre dos veces.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthFilter(JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring(7);
            try {
                Claims claims = jwtService.validar(token, JwtService.TIPO_ACCESS);
                UserDetails usuario = usuarioDetailsService.loadUserByUsername(claims.getSubject());

                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);

            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                // Token vencido, alterado o de un usuario que ya no existe: queda sin autenticar
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}