package com.petsalud.backendpetsalud.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.petsalud.backendpetsalud.security.JwtAuthenticationEntryPoint;
import com.petsalud.backendpetsalud.security.UsuarioDetailsService;
import com.petsalud.backendpetsalud.security.monitor.ActividadSospechosaService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthFilter: el "guardia" que revisa el token en cada petición, antes de llegar al controller.
 * Si el token es válido, marca al usuario como autenticado con su rol.
 * Si es falso, vencido o el usuario ya no existe, responde 401 en formato JSON.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private ActividadSospechosaService actividadSospechosa;

    /**
     * Se ejecuta una vez por petición:
     * 1) busca el header "Authorization: Bearer ...";  2) lee y valida el token;
     * 3) carga el usuario desde la BD;  4) confirma que el token es suyo y está vigente;
     * 5) lo anota en el SecurityContext para que SecurityConfig y @PreAuthorize revisen su rol.
     * Si no viene token, deja pasar sin marcar: SecurityConfig decidirá si la ruta es pública.
     */
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
        String email;
        try {
            // 2. ¿De quién es? (verifica firma, emisor, audiencia y vencimiento)
            email = jwtUtil.getUsernameFromToken(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 3. ¿Existe? (si el usuario fue eliminado, lanza UsernameNotFoundException)
                UserDetails user = usuarioDetailsService.loadUserByUsername(email);

                // 4. ¿Es válida?
                if (jwtUtil.isTokenValid(token, user)) {
                    // 5. Marcar como autenticado
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException e) {
            actividadSospechosa.registrarTokenInvalido(request, e.getClass().getSimpleName());
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("Token inválido o expirado"));
            return;
        } catch (UsernameNotFoundException e) {
            actividadSospechosa.registrarTokenInvalido(request, "usuario eliminado"); 
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("El usuario del token ya no existe"));
            return;
        }

        // Dejar que la petición siga su camino (al controller)
        filterChain.doFilter(request, response);
    }
}