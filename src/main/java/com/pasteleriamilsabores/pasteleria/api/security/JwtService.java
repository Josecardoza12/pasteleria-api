package com.pasteleriamilsabores.pasteleria.api.security;

import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Crea y valida los JWT.
 * - Access token: dura 15 minutos y viaja en cada petición (Authorization: Bearer ...).
 * - Refresh token: dura 7 días y solo sirve para pedir tokens nuevos en /auth/refresh.
 * Ambos van firmados con HMAC usando la clave jwt.secret: si alguien los modifica, la firma no calza.
 */
@Service
public class JwtService {

    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey clave;
    private final long minutosAccess;
    private final long diasRefresh;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.access-minutos}") long minutosAccess,
                      @Value("${jwt.refresh-dias}") long diasRefresh) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.minutosAccess = minutosAccess;
        this.diasRefresh = diasRefresh;
    }

    public String generarAccessToken(Usuario usuario) {
        return generar(usuario, TIPO_ACCESS, Duration.ofMinutes(minutosAccess));
    }

    public String generarRefreshToken(Usuario usuario) {
        return generar(usuario, TIPO_REFRESH, Duration.ofDays(diasRefresh));
    }

    /** Segundos que dura el access token (se envía como "expiraEn"). */
    public long getSegundosAccess() {
        return minutosAccess * 60;
    }

    /**
     * Verifica la firma, el vencimiento y el tipo del token.
     * Si algo falla lanza JwtException (token vencido, alterado o del tipo equivocado).
     */
    public Claims validar(String token, String tipoEsperado) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!tipoEsperado.equals(claims.get("tipo", String.class))) {
            throw new JwtException("El token no es de tipo " + tipoEsperado);
        }
        return claims;
    }

    private String generar(Usuario usuario, String tipo, Duration duracion) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getCorreo())                 // quién es
                .claim("rol", usuario.getRol().name())        // qué puede hacer
                .claim("tipo", tipo)                          // access o refresh
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave)
                .compact();
    }
}