package com.example.Alojamientos.securityLayer;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long expirationMs;

    // ─────────────────────────────────────────
    // GENERACIÓN DE TOKEN
    // ─────────────────────────────────────────

    /**
     * Genera un token JWT para el usuario con claims adicionales
     */
    public String generarToken(String email, Map<String, Object> claimsExtra) {
        return Jwts.builder()
                .setClaims(claimsExtra)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generarToken(String email) {
        return generarToken(email, new HashMap<>());
    }

    public String generarTokenConRol(String email, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol);
        return generarToken(email, claims);
    }

    public String generarRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ─────────────────────────────────────────
    // EXTRACCIÓN DE CLAIMS
    // ─────────────────────────────────────────

    /**
     * Extrae el email (subject) del token
     */
    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token
     */
    public Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae el rol del token (si fue incluido al generarlo)
     */
    public String extraerRol(String token) {
        Claims claims = extraerTodosLosClaims(token);
        return claims.get("rol", String.class);
    }

    /**
     * Extrae cualquier claim usando una función lambda
     * Ejemplo: extraerClaim(token, Claims::getSubject)
     */
    public <T> T extraerClaim(String token, Function<Claims, T> resolvedor) {
        final Claims claims = extraerTodosLosClaims(token);
        return resolvedor.apply(claims);
    }

    /**
     * Extrae todos los claims del token
     */
    public Claims extraerTodosLosClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ─────────────────────────────────────────
    // VALIDACIÓN DEL TOKEN
    // ─────────────────────────────────────────

    /**
     * Valida si el token pertenece al email dado y no ha expirado
     */
    public boolean esTokenValido(String token, String email) {
        final String emailExtraido = extraerEmail(token);
        return emailExtraido.equals(email) && !estaExpirado(token);
    }

    /**
     * Verifica si el token ha expirado
     */
    public boolean estaExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    /**
     * Verifica solo la estructura y firma del token (sin comparar usuario)
     */
    public boolean esTokenEstructuralmenteValido(String token) {
        try {
            extraerTodosLosClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ─────────────────────────────────────────
    // UTILIDADES INTERNAS
    // ─────────────────────────────────────────

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}