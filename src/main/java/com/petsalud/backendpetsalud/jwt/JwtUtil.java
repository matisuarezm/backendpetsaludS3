package com.petsalud.backendpetsalud.jwt;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * JwtUtil: la "máquina de pulseras" (tokens JWT).
 * Fabrica tokens firmados en el login y los valida en cada petición.
 * La clave, el emisor, la audiencia y la duración vienen de application.properties / .env.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.issuer}")
    private String issuer;        // quién emite el token

    @Value("${jwt.audience}")
    private String audience;      // para quién es el token

    @Value("${jwt.expiration-ms}")
    private long expirationMs;    // duración

    /**
     * Fabrica el token del usuario con: id único (jti), quién lo emite (iss), para quién es (aud),
     * el email (sub), fecha de creación (iat) y de vencimiento (exp). Lo firma con la clave secreta (HS256).
     */
    public String getToken(UserDetails user) {
        Date ahora = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())   // "jti": id único de esta pulsera
                .issuer(issuer)                      // "iss": la emitió PetSalud
                .audience().add(audience).and()      // "aud": sirve solo para esta API
                .subject(user.getUsername())         // "sub": el email
                .issuedAt(ahora)                     // "iat"
                .expiration(new Date(ahora.getTime() + expirationMs)) // "exp"
                .signWith(getKey(), Jwts.SIG.HS256)  // el sello
                .compact();
    }

    /**
     * Convierte la clave secreta (texto Base64 del .env) en la llave usada para firmar y verificar.
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    /**
     * Devuelve el email guardado en el token. Antes valida firma, emisor, audiencia y vencimiento:
     * si algo falla lanza JwtException (el filtro la convierte en 401).
     */
    public String getUsernameFromToken(String token) {
        return getAllClaims(token).getSubject();
    }

    /**
     * Confirma que el token pertenece a ese usuario y que todavía no vence.
     */
    public boolean isTokenValid(String token, UserDetails user) {
        Claims claims = getAllClaims(token);
        return claims.getSubject().equals(user.getUsername())
                && claims.getExpiration().after(new Date());
    }

    // Lee y VALIDA: firma, emisor y audiencia. Si algo no calza, lanza JwtException
    /**
     * Abre y verifica el token: firma con nuestra llave, emisor y audiencia correctos.
     * Si alguien lo modificó o ya venció, lanza una excepción y no se lee nada.
     */
    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}