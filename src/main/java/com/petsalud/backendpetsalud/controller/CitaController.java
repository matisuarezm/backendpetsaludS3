package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Cita;
import com.petsalud.backendpetsalud.service.CitaService;
import com.petsalud.backendpetsalud.validation.Patrones;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PreAuthorize("hasAnyAuthority('EMPLEADO','GERENTE') or @propiedad.esDuenoDeMascota(#mascotaId, authentication)") // agendarCita: solo para mis mascotas
    @PostMapping
    public ResponseEntity<Cita> agendarCita(
            @RequestParam Long mascotaId,
            @RequestParam Long veterinarioId,
            //Spring convierte la fecha solo (formato 2026-11-20T10:30:00); si viene mal, responde 400
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @FutureOrPresent(message = "La cita no puede ser en el pasado") LocalDateTime fechaHora,
            // Validación del texto libre
            @RequestParam @NotBlank @Size(max = 200)
            @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "Contiene caracteres no permitidos") String motivo) {
        Cita nuevaCita = citaService.agendarCita(mascotaId, veterinarioId, fechaHora, motivo);
        return ResponseEntity.ok(nuevaCita);
    }

    @PreAuthorize("hasAnyAuthority('EMPLEADO','GERENTE') or @propiedad.esDuenoDeCita(#id, authentication)")
    @GetMapping("/{id}")                                // obtenerCitaPorId
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        return citaService.obtenerCitaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Cita> actualizarEstadoCita(
            @PathVariable Long id,
            // Lista blanca, solo se aceptan estos 3 valores
            @RequestParam @Pattern(regexp = "^(Agendada|Cancelada|Completada)$",
                    message = "Estado inválido: use Agendada, Cancelada o Completada") String estado) {
        Cita citaActualizada = citaService.actualizarEstadoCita(id, estado);
        return ResponseEntity.ok(citaActualizada);
    }

    @PreAuthorize("hasAuthority('GERENTE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('EMPLEADO','GERENTE') or @propiedad.esDuenoDeMascota(#mascotaId, authentication)")
    @GetMapping("/mascota/{mascotaId}")                 // listarCitasPorMascota
    public ResponseEntity<List<Cita>> listarCitasPorMascota(@PathVariable Long mascotaId) {
        List<Cita> citas = citaService.listarCitasPorMascota(mascotaId);
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/veterinario/{veterinarioId}")
    public ResponseEntity<List<Cita>> listarCitasPorVeterinario(@PathVariable Long veterinarioId) {
        List<Cita> citas = citaService.listarCitasPorVeterinario(veterinarioId);
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/entre-fechas")
    public ResponseEntity<List<Cita>> listarCitasEntreFechas(
            // Igual que arriba, conversión automática y 400 si la fecha está mal
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<Cita> citas = citaService.listarCitasEntreFechas(inicio, fin);
        return ResponseEntity.ok(citas);
    }
}