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

    // Fabrica la pulsera
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

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String getUsernameFromToken(String token) {
        return getAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        Claims claims = getAllClaims(token);
        return claims.getSubject().equals(user.getUsername())
                && claims.getExpiration().after(new Date());
    }

    // Lee y VALIDA: firma, emisor y audiencia. Si algo no calza, lanza JwtException
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