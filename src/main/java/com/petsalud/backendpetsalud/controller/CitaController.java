package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Cita;
import com.petsalud.backendpetsalud.service.CitaService;
import com.petsalud.backendpetsalud.validation.Patrones;          // ← NUEVO
import jakarta.validation.constraints.FutureOrPresent;             // ← NUEVO
import jakarta.validation.constraints.NotBlank;                    // ← NUEVO
import jakarta.validation.constraints.Pattern;                     // ← NUEVO
import jakarta.validation.constraints.Size;                        // ← NUEVO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;       // ← NUEVO
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<Cita> agendarCita(
            @RequestParam Long mascotaId,
            @RequestParam Long veterinarioId,
            // ← CAMBIO: Spring convierte la fecha solo (formato 2026-11-20T10:30:00); si viene mal, responde 400
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @FutureOrPresent(message = "La cita no puede ser en el pasado") LocalDateTime fechaHora,
            // ← NUEVO: validación del texto libre
            @RequestParam @NotBlank @Size(max = 200)
            @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "Contiene caracteres no permitidos") String motivo) {
        Cita nuevaCita = citaService.agendarCita(mascotaId, veterinarioId, fechaHora, motivo);
        return ResponseEntity.ok(nuevaCita);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        return citaService.obtenerCitaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Cita> actualizarEstadoCita(
            @PathVariable Long id,
            // ← NUEVO: lista blanca, solo se aceptan estos 3 valores
            @RequestParam @Pattern(regexp = "^(Agendada|Cancelada|Completada)$",
                    message = "Estado inválido: use Agendada, Cancelada o Completada") String estado) {
        Cita citaActualizada = citaService.actualizarEstadoCita(id, estado);
        return ResponseEntity.ok(citaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mascota/{mascotaId}")
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
            // ← CAMBIO: igual que arriba, conversión automática y 400 si la fecha está mal
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<Cita> citas = citaService.listarCitasEntreFechas(inicio, fin);
        return ResponseEntity.ok(citas);
    }
}