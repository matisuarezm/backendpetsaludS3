package com.petsalud.backendpetsalud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.petsalud.backendpetsalud.dto.AuthResponse;
import com.petsalud.backendpetsalud.dto.LoginRequest;
import com.petsalud.backendpetsalud.jwt.JwtServices;
import com.petsalud.backendpetsalud.security.UsuarioDetailsService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private JwtServices jwtServices;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        // 1. Validar email y clave (si la clave es incorrecta, lanza excepción y se corta aquí)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Cargar el usuario desde la BD
        UserDetails user = usuarioDetailsService.loadUserByUsername(request.getEmail());

        // 3. Fabricar el token firmado
        String token = jwtServices.getToken(user);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}