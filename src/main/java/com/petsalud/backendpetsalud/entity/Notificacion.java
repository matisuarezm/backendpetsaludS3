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
@Table(name = "notificaciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 500)
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "Contiene caracteres no permitidos")
    private String mensaje;

    @Pattern(regexp = "^(Promoción|Recordatorio|Evento)$", message = "Tipo inválido")
    private String tipo;

    private LocalDateTime fecha;

    private Boolean leida;

    // Relaciones
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
