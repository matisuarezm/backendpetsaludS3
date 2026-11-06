package com.petsalud.backendpetsalud.dto;

import com.petsalud.backendpetsalud.validation.Patrones;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Solo los campos que un usuario puede editar. No incluye email, password ni rol.
@Data
public class ActualizarUsuarioRequest {

    @NotBlank @Size(min = 2, max = 50)
    @Pattern(regexp = Patrones.NOMBRE, message = "Solo puede contener letras")
    private String nombre;

    @NotBlank @Size(min = 2, max = 50)
    @Pattern(regexp = Patrones.NOMBRE, message = "Solo puede contener letras")
    private String apellido;

    @Pattern(regexp = Patrones.TELEFONO, message = "Teléfono inválido (8 a 12 dígitos)")
    private String telefono;

    @Size(max = 150)
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "Contiene caracteres no permitidos")
    private String direccion;
}