package com.petsalud.backendpetsalud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.petsalud.backendpetsalud.dto.AuthResponse;
import com.petsalud.backendpetsalud.dto.LoginRequest;
import com.petsalud.backendpetsalud.jwt.JwtUtil;
import com.petsalud.backendpetsalud.security.UsuarioDetailsService;

import jakarta.validation.Valid;

/**
 * Endpoint público de login (/auth/login): recibe email y contraseña y, si son correctos,
 * entrega el token JWT que el cliente usará en las siguientes peticiones.
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

    /**
     * Primero, @Valid revisa el formato del email. Luego:
     * 1) AuthenticationManager valida email y clave (si fallan: 401 "Email o contraseña incorrectos");
     * 2) se carga el usuario;  3) JwtUtil fabrica el token y se devuelve en AuthResponse.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request)  {

        // 1. Validar email y clave (si la clave es incorrecta, lanza excepción y se corta aquí)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Cargar el usuario desde la BD
        UserDetails user = usuarioDetailsService.loadUserByUsername(request.getEmail());

        // 3. Fabricar el token firmado
        String token = jwtUtil.getToken(user);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}