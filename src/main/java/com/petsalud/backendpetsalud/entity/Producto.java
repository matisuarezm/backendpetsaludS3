package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.petsalud.backendpetsalud.validation.Patrones;

@Entity
@Table(name = "productos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 2, max = 100)
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "Contiene caracteres no permitidos")
    private String nombre;

    @Size(max = 500)
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "Contiene caracteres no permitidos")
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "9999999.99", message = "Precio fuera de rango")
    @Digits(integer = 7, fraction = 2)
    private BigDecimal precio;

    @NotNull
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Max(value = 100000, message = "Stock fuera de rango")
    private Integer stock;

    @NotBlank @Size(max = 50)
    @Pattern(regexp = Patrones.NOMBRE, message = "Solo puede contener letras")
    private String categoria;
}

