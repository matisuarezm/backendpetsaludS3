package com.petsalud.backendpetsalud.security.monitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Monitoreo de actividad sospechosa.
 * Escribe en el log (con fecha y hora) los logins fallidos, bloqueos, tokens inválidos
 * y accesos denegados. Además BLOQUEA el login por 15 minutos tras 5 intentos fallidos.
 * Nunca registra contraseñas ni tokens, y oculta parte del email.
 */
@Component
public class ActividadSospechosaService {

    // Todas las líneas salen con el nombre "SEGURIDAD": así se filtran fácil en el log
    private static final Logger log = LoggerFactory.getLogger("SEGURIDAD");

    private static final int MAX_INTENTOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;
    private static final long VENTANA_MS = MINUTOS_BLOQUEO * 60 * 1000L;

    // Para cada "email|ip" guarda la hora de cada intento fallido reciente
    private final Map<String, Deque<Long>> intentosFallidos = new ConcurrentHashMap<>();

    /** ¿Este email, desde esta IP, superó los 5 intentos fallidos en los últimos 15 minutos? */
    public boolean estaBloqueado(String email, HttpServletRequest request) {
        Deque<Long> intentos = intentosFallidos.get(clave(email, request));
        if (intentos == null) {
            return false;
        }
        synchronized (intentos) {
            borrarAntiguos(intentos);
            return intentos.size() >= MAX_INTENTOS;
        }
    }

    /** Suma un intento fallido. Al llegar a 5, deja registrado el bloqueo. */
    public void registrarLoginFallido(String email, HttpServletRequest request) {
        Deque<Long> intentos = intentosFallidos.computeIfAbsent(clave(email, request), k -> new ArrayDeque<>());
        int total;
        synchronized (intentos) {
            intentos.addLast(System.currentTimeMillis());
            borrarAntiguos(intentos);
            total = intentos.size();
        }
        log.warn("LOGIN_FALLIDO email={} ip={} intentos={}/{}", ocultarEmail(email), ip(request), total, MAX_INTENTOS);
        if (total >= MAX_INTENTOS) {
            log.warn("CUENTA_BLOQUEADA email={} ip={} minutos={}", ocultarEmail(email), ip(request), MINUTOS_BLOQUEO);
        }
    }

    /** Login correcto: borra los intentos fallidos acumulados. */
    public void registrarLoginExitoso(String email, HttpServletRequest request) {
        intentosFallidos.remove(clave(email, request));
        log.info("LOGIN_EXITOSO email={} ip={}", ocultarEmail(email), ip(request));
    }

    /** Alguien intentó entrar mientras estaba bloqueado (aunque traiga la clave correcta). */
    public void registrarLoginBloqueado(String email, HttpServletRequest request) {
        log.warn("LOGIN_RECHAZADO_POR_BLOQUEO email={} ip={}", ocultarEmail(email), ip(request));
    }

    /** Token falso, alterado, vencido, o de un usuario eliminado. */
    public void registrarTokenInvalido(HttpServletRequest request, String motivo) {
        log.warn("TOKEN_INVALIDO ip={} metodo={} ruta={} motivo={}",
                ip(request), request.getMethod(), limpiar(request.getRequestURI()), limpiar(motivo));
    }

    /** Petición a una ruta protegida sin token. */
    public void registrarSinToken(HttpServletRequest request) {
        log.info("ACCESO_SIN_TOKEN ip={} metodo={} ruta={}",
                ip(request), request.getMethod(), limpiar(request.getRequestURI()));
    }

    /** Usuario identificado que intentó entrar donde su rol no puede, o a un recurso ajeno. */
    public void registrarAccesoDenegado(HttpServletRequest request) {
        String usuario = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse("desconocido");
        log.warn("ACCESO_DENEGADO usuario={} ip={} metodo={} ruta={}",
                ocultarEmail(usuario), ip(request), request.getMethod(), limpiar(request.getRequestURI()));
    }

    // ---------- ayudantes ----------

    /** Quita los intentos más antiguos que 15 minutos. */
    private void borrarAntiguos(Deque<Long> intentos) {
        long limite = System.currentTimeMillis() - VENTANA_MS;
        while (!intentos.isEmpty() && intentos.peekFirst() < limite) {
            intentos.pollFirst();
        }
    }

    /** Identifica al que intenta: email (en minúsculas) + IP. */
    private String clave(String email, HttpServletRequest request) {
        String e = email == null ? "" : email.trim().toLowerCase();
        return e + "|" + ip(request);
    }

    /** IP real de la conexión. No usamos X-Forwarded-For porque el cliente lo puede falsificar. */
    private String ip(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    /** Evita que alguien "escriba líneas falsas" en el log metiendo saltos de línea en un dato. */
    private String limpiar(String texto) {
        return texto == null ? "" : texto.replaceAll("[\\r\\n\\t]", "_");
    }

    /** "ana@petsalud.cl" → "an***@petsalud.cl" (minimización de datos personales). */
    private String ocultarEmail(String email) {
        String e = limpiar(email);
        int arroba = e.indexOf('@');
        if (arroba < 0) {
            return e.length() <= 2 ? "***" : e.substring(0, 2) + "***";
        }
        if (arroba <= 2) {
            return "***" + e.substring(arroba);
        }
        return e.substring(0, 2) + "***" + e.substring(arroba);
    }
}