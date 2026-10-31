package com.petsalud.backendpetsalud.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service 
public class JwtServices {

    @Value("${jwt.secret}")
    private String secretKey;

    //Fabrica el token con email, fecha y vencimiento, y lo firma
    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user);
    }

    private String getToken(Map<String,Object> extraClaims, UserDetails user) {
        return Jwts
                .builder()
                .setClaims(extraClaims) //datos extras podria ir el ROL por ejemplo
                .setSubject(user.getUsername()) //"sub": el email del usuario
                .setIssuedAt(new Date(System.currentTimeMillis())) // "iat": cuándo se creó
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*60)) //Duracion de 1 hora para el token (Ej:1000*60*60*24 Duracion de 24hrs.)
                .signWith(getKey(), SignatureAlgorithm.HS256) // firma con tu clave secreta → el sello
                .compact();  // lo convierte en el texto xxx.yyy.zzz
    }

    //Convierte tu clave secreta en una llave criptográfica
    private Key getKey() {
        byte[] keyBytes=Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //Lee el email del payload (y de paso verifica la firma)
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    //¿El email calza con el usuario y no está vencido?
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username=getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername())&& !isTokenExpired(token));
    }

    private Claims getAllClaims(String token)
    {
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey()) // "verifica con MI llave"
                .build()
                .parseClaimsJws(token) // ← aquí se valida la firma
                .getBody(); // devuelve el payload (los claims)
    }

    public <T> T getClaim(String token, Function<Claims,T> claimsResolver)
    {
        final Claims claims=getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpiration(String token)
    {
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token)
    {
        return getExpiration(token).before(new Date());
    }

}
