package com.petsalud.backendpetsalud.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsalud.backendpetsalud.dto.ErrorResponse;
import com.petsalud.backendpetsalud.security.monitor.ActividadSospechosaService;   // ← NUEVO
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Responde el error 403 (autenticado pero SIN permiso) con el formato JSON común.
 * Spring lo usa cuando el rol del usuario no cumple una regla de SecurityConfig.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired                                                   // ← NUEVO
    private ActividadSospechosaService actividadSospechosa;      // ← NUEVO: el "anotador" del log

    /**
     * Arma la respuesta 403 con el mensaje "No tiene permisos para este recurso".
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        // ← NUEVO: anotamos en el log quién intentó entrar y a dónde
        actividadSospechosa.registrarAccesoDenegado(request);

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
                ErrorResponse.of(HttpStatus.FORBIDDEN, "No tiene permisos para este recurso",
                        request.getRequestURI()));
    }
}