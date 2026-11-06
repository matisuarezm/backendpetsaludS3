package com.petsalud.backendpetsalud.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.petsalud.backendpetsalud.repository.CitaRepository;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.NotificacionRepository;
import com.petsalud.backendpetsalud.repository.RecompensaRepository;
import com.petsalud.backendpetsalud.repository.VentaRepository;

/**
 * Propiedad: responde "¿este recurso le pertenece al usuario logueado?".
 * La usan las anotaciones @PreAuthorize de los controllers, con el nombre "@propiedad".
 * Si el recurso no existe, responde false: así un cliente no puede averiguar
 * qué ids existen probando números.
 */
@Component("propiedad")
public class Propiedad {

    @Autowired private MascotaRepository mascotaRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private RecompensaRepository recompensaRepository;

    /** Saca el id del usuario logueado desde el token ya validado por el filtro. */
    private Long miId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails usuario) {
            return usuario.getId();
        }
        return null;
    }

    /** ¿El id pedido es el mío? (para /usuarios/{id} y las rutas /usuario/{usuarioId}) */
    public boolean esUsuario(Long usuarioId, Authentication auth) {
        Long yo = miId(auth);
        return yo != null && yo.equals(usuarioId);
    }

    /** ¿Esta mascota es mía? */
    public boolean esDuenoDeMascota(Long mascotaId, Authentication auth) {
        Long yo = miId(auth);
        return yo != null && mascotaRepository.findById(mascotaId)
                .map(m -> m.getPropietario() != null && yo.equals(m.getPropietario().getId()))
                .orElse(false);
    }

    /** ¿Esta cita es de una de mis mascotas? */
    public boolean esDuenoDeCita(Long citaId, Authentication auth) {
        Long yo = miId(auth);
        return yo != null && citaRepository.findById(citaId)
                .map(c -> c.getMascota() != null && c.getMascota().getPropietario() != null
                        && yo.equals(c.getMascota().getPropietario().getId()))
                .orElse(false);
    }

    /** ¿Esta compra es mía? */
    public boolean esDuenoDeVenta(Long ventaId, Authentication auth) {
        Long yo = miId(auth);
        return yo != null && ventaRepository.findById(ventaId)
                .map(v -> v.getUsuario() != null && yo.equals(v.getUsuario().getId()))
                .orElse(false);
    }

    /** ¿Esta notificación es mía? */
    public boolean esDuenoDeNotificacion(Long notificacionId, Authentication auth) {
        Long yo = miId(auth);
        return yo != null && notificacionRepository.findById(notificacionId)
                .map(n -> n.getUsuario() != null && yo.equals(n.getUsuario().getId()))
                .orElse(false);
    }

    /** ¿Esta recompensa es mía? */
    public boolean esDuenoDeRecompensa(Long recompensaId, Authentication auth) {
        Long yo = miId(auth);
        return yo != null && recompensaRepository.findById(recompensaId)
                .map(r -> r.getUsuario() != null && yo.equals(r.getUsuario().getId()))
                .orElse(false);
    }
}