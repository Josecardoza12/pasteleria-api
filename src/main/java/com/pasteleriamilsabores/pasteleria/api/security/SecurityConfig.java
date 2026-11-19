package com.pasteleriamilsabores.pasteleria.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Reglas de seguridad de la API:
 * - Sin sesiones en el servidor (STATELESS): cada petición trae su JWT.
 * - CORS solo para el frontend en http://localhost:5173.
 * - Qué rutas son públicas y qué rol necesita cada una (ver CONTRATO_API.md).
 */
@Configuration
@EnableMethodSecurity   // permite usar @PreAuthorize en los controladores
public class SecurityConfig {

    private final EntradaNoAutorizada entradaNoAutorizada;
    private final AccesoDenegado accesoDenegado;
    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(EntradaNoAutorizada entradaNoAutorizada, AccesoDenegado accesoDenegado,
                          JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.entradaNoAutorizada = entradaNoAutorizada;
        this.accesoDenegado = accesoDenegado;
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // La API no usa cookies de sesión, así que CSRF no aplica
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sin formulario de login ni ventana de usuario/contraseña del navegador
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entradaNoAutorizada)   // 401
                        .accessDeniedHandler(accesoDenegado))           // 403
                .authorizeHttpRequests(auth -> auth
                        // Preflight de CORS que hace el navegador
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger y la página de error de Spring
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()

                        // ---------- Autenticación (José) ----------
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login", "/api/v1/auth/registro", "/api/v1/auth/refresh").permitAll()

                        // ---------- Usuarios (José): solo administrador ----------
                        .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")

                        // ---------- Productos y categorías (Catalina) ----------
                        // stock-critico va ANTES del GET público para que no quede abierto
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/stock-critico")
                        .hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/productos", "/api/v1/productos/**",
                                "/api/v1/categorias", "/api/v1/categorias/**").permitAll()
                        .requestMatchers(
                                "/api/v1/productos", "/api/v1/productos/**",
                                "/api/v1/categorias", "/api/v1/categorias/**").hasRole("ADMINISTRADOR")

                        // ---------- Boletas (Juan) ----------
                        .requestMatchers(HttpMethod.POST, "/api/v1/boletas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas/mias").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        // /boletas/{id}: cualquiera con sesión; que sea el dueño lo revisa el servicio (403)
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas/*").authenticated()

                        // Todo lo demás (por ejemplo /auth/me) exige sesión
                        .anyRequest().authenticated());

        // Filtro JWT: lee el token antes del filtro de usuario y contraseña de Spring
        http.addFilterBefore(new JwtAuthFilter(jwtService, usuarioDetailsService),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Lo usa el login para comprobar correo y contraseña (BCrypt). */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    /** CORS: solo el frontend de Vite puede llamar a la API desde el navegador. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}