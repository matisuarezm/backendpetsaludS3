package com.petsalud.backendpetsalud.security;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.petsalud.backendpetsalud.jwt.JwtAuthFilter;

/**
 * SecurityConfig: el "reglamento" de seguridad de la API.
 * Define qué rutas son públicas, qué rol necesita cada ruta, que no se usen sesiones (stateless),
 * quién responde los errores 401/403 y en qué lugar se ubica el filtro JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity 
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler accessDeniedHandler;

    /**
     * Arma la cadena de seguridad que revisa CADA petición.
     * Las reglas se leen de arriba hacia abajo y se aplica la primera que calce:
     * por eso las más específicas van primero y "anyRequest().authenticated()" al final.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                // 1. PÚBLICAS: no requieren login
                .requestMatchers(HttpMethod.POST, "/api/usuarios/registro").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/error").permitAll()

                // 2. SOLO GERENTE
                // Borrar cualquier recurso
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasAuthority("GERENTE")
                // Listar todos los usuarios (solo la lista, no /{id})
                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAuthority("GERENTE")
                // Crear y modificar productos (inventario y precios)
                .requestMatchers(HttpMethod.POST, "/api/productos").hasAuthority("GERENTE")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAuthority("GERENTE")
                .requestMatchers(HttpMethod.GET, "/api/productos/stock-bajo").hasAuthority("GERENTE")
                // Reporte de todas las ventas
                .requestMatchers(HttpMethod.GET, "/api/ventas").hasAuthority("GERENTE")
                // Enviar notificaciones y asignar recompensas
                .requestMatchers(HttpMethod.POST, "/api/notificaciones/**").hasAuthority("GERENTE")
                .requestMatchers(HttpMethod.POST, "/api/recompensas/**").hasAuthority("GERENTE")

                // 3. EMPLEADO y GERENTE
                // El cliente puede ver el historial de SU mascota: @PreAuthorize revisa que sea el dueño
                .requestMatchers(HttpMethod.GET, "/api/expedientes/mascota/*").authenticated()
                // Expedientes médicos (datos clínicos sensibles)
                .requestMatchers("/api/expedientes/**").hasAnyAuthority("EMPLEADO", "GERENTE")
                // Gestión de citas del lado clínico
                .requestMatchers(HttpMethod.PUT, "/api/citas/*/estado").hasAnyAuthority("EMPLEADO", "GERENTE")
                .requestMatchers(HttpMethod.GET, "/api/citas/veterinario/**").hasAnyAuthority("EMPLEADO", "GERENTE")
                .requestMatchers(HttpMethod.GET, "/api/citas/entre-fechas").hasAnyAuthority("EMPLEADO", "GERENTE")

                // 4. RED DE SEGURIDAD: todo lo demás requiere estar logueado
                .anyRequest().authenticated()
        )
                .userDetailsService(usuarioDetailsService)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)  // 401
                    .accessDeniedHandler(accessDeniedHandler)            // 403
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); //Agregamos el filtro para autenticar con el token

        return http.build();
    }

    /**
     * Entrega el algoritmo BCrypt: cifra la contraseña al registrar y la compara en el login.
     * Es el mismo objeto en ambos lados, por eso las claves calzan.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Necesario para la autenticación en AuthController
    /**
     * Entrega el "revisor de credenciales" que usa AuthController en el login:
     * busca al usuario por email y compara la clave con BCrypt.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
