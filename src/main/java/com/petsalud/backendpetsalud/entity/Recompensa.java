package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.petsalud.backendpetsalud.validation.Patrones;

@Entity
@Table(name = "recompensas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recompensa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(value = 1, message = "Mínimo 1 punto")
    @Max(value = 10000, message = "Máximo 10000 puntos")
    private Integer puntos;

    @Size(max = 200)
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "Contiene caracteres no permitidos")
    private String descripcion;

    private LocalDateTime fecha;

    // Relaciones
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
