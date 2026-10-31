package com.petsalud.backendpetsalud.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.petsalud.backendpetsalud.security.UsuarioDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtServices jwtServices;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. ¿Trae pulsera? Buscar el header "Authorization: Bearer xxxxx"
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No trae token: dejarla seguir sin marcar (SecurityConfig decidirá)
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // quita "Bearer " (7 caracteres)

        // 2. ¿De quién es? Leer el email del token (aquí se verifica la firma)
        String email = jwtServices.getUsernameFromToken(token);

        // Solo si hay email y la petición aún no está marcada como autenticada
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 3. ¿Existe? Cargar el usuario desde la BD
            UserDetails user = usuarioDetailsService.loadUserByUsername(email);

            // 4. ¿Es válida? Email calza y no está vencida
            if (jwtServices.isTokenValid(token, user)) {

                // 5. Marcar la petición como autenticada, con sus roles
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Dejar que la petición siga su camino (al controller)
        filterChain.doFilter(request, response);
    }
}