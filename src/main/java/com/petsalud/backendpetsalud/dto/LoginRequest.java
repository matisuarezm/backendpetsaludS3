package com.petsalud.backendpetsalud.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Datos que llegan al login: email y contraseña.
 * Las anotaciones validan el formato antes de intentar autenticar.
 */
@Data
public class LoginRequest {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(max = 72)
    private String password;
}