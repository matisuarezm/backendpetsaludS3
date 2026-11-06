package com.petsalud.backendpetsalud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petsalud.backendpetsalud.validation.Patrones;

@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Size(max = 100)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = Patrones.PASSWORD, message = "Debe tener 8 a 72 caracteres, con letras y números, sin espacios")
    private String password;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    // Relaciones
    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL)
    private List<Mascota> mascotas;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Venta> ventas;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Recompensa> recompensas;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Notificacion> notificaciones;
}