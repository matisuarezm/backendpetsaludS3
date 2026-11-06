package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Notificacion;
import com.petsalud.backendpetsalud.service.NotificacionService;
import jakarta.validation.Valid;                                   // ← NUEVO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<Notificacion> enviarNotificacion(@PathVariable Long usuarioId,
                                                           @Valid @RequestBody Notificacion notificacion) { // ← NUEVO: @Valid
        Notificacion nuevaNotificacion = notificacionService.enviarNotificacion(usuarioId, notificacion);
        return ResponseEntity.ok(nuevaNotificacion);
    }

    @PreAuthorize("hasAuthority('GERENTE') or @propiedad.esDuenoDeNotificacion(#id, authentication)")
    @GetMapping("/{id}")                                // obtenerNotificacionPorId
    public ResponseEntity<Notificacion> obtenerNotificacionPorId(@PathVariable Long id) {
        return notificacionService.obtenerNotificacionPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('GERENTE') or @propiedad.esUsuario(#usuarioId, authentication)")
    @GetMapping("/usuario/{usuarioId}")                 // listarNotificacionesPorUsuario
    public ResponseEntity<List<Notificacion>> listarNotificacionesPorUsuario(@PathVariable Long usuarioId) {
        List<Notificacion> notificaciones = notificacionService.listarNotificacionesPorUsuario(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    @PreAuthorize("hasAuthority('GERENTE') or @propiedad.esUsuario(#usuarioId, authentication)")
    @GetMapping("/usuario/{usuarioId}/no-leidas")       // listarNotificacionesNoLeidas
    public ResponseEntity<List<Notificacion>> listarNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        List<Notificacion> notificaciones = notificacionService.listarNotificacionesNoLeidas(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    @PreAuthorize("hasAuthority('GERENTE') or @propiedad.esDuenoDeNotificacion(#id, authentication)")
    @PutMapping("/{id}/leida")                          // marcarComoLeida
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}