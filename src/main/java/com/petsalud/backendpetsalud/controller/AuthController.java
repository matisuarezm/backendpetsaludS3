package com.petsalud.backendpetsalud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.petsalud.backendpetsalud.dto.AuthResponse;
import com.petsalud.backendpetsalud.dto.LoginRequest;
import com.petsalud.backendpetsalud.exception.CuentaBloqueadaException;
import com.petsalud.backendpetsalud.jwt.JwtUtil;
import com.petsalud.backendpetsalud.security.UsuarioDetailsService;
import com.petsalud.backendpetsalud.security.monitor.ActividadSospechosaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Endpoint público de login (/auth/login): recibe email y contraseña y, si son correctos,
 * entrega el token JWT. Registra cada intento y bloquea tras 5 fallos en 15 minutos.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ActividadSospechosaService actividadSospechosa;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {

        // 0. ¿Está bloqueado? Se revisa ANTES de mirar la clave: aunque sea correcta, no entra
        if (actividadSospechosa.estaBloqueado(request.getEmail(), httpRequest)) {
            actividadSospechosa.registrarLoginBloqueado(request.getEmail(), httpRequest);
            throw new CuentaBloqueadaException();   // → 429
        }

        // 1. Validar email y clave. Si fallan: se registra el intento y se responde 401
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            actividadSospechosa.registrarLoginFallido(request.getEmail(), httpRequest);
            throw e;   // GlobalExceptionHandler responde 401 "Email o contraseña incorrectos"
        }

        // Login correcto: se reinicia el contador de fallos
        actividadSospechosa.registrarLoginExitoso(request.getEmail(), httpRequest);

        // 2. Cargar el usuario y 3. fabricar el token
        UserDetails user = usuarioDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.getToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}