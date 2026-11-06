package com.petsalud.backendpetsalud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta del login: SOLO el token. No incluye datos del usuario.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
}