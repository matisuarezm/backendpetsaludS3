package com.petsalud.backendpetsalud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import com.petsalud.backendpetsalud.validation.Patrones;

@Entity
@Table(name = "mascotas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 50)
    @Pattern(regexp = Patrones.NOMBRE, message = "Solo puede contener letras")
    private String nombre;

    @NotBlank @Size(max = 30)
    @Pattern(regexp = Patrones.NOMBRE, message = "Solo puede contener letras")
    private String especie;

    @Size(max = 50)
    @Pattern(regexp = Patrones.NOMBRE, message = "Solo puede contener letras")
    private String raza;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    private LocalDate fechaNacimiento;

    @Pattern(regexp = "^(Macho|Hembra)$", message = "Debe ser Macho o Hembra")
    private String sexo;

    // Relaciones
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario propietario;

    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    private List<Cita> citas;

    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    private List<ExpedienteMedico> expedientesMedicos;
}
