package com.petsalud.backendpetsalud.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsalud.backendpetsalud.dto.ErrorResponse;
import com.petsalud.backendpetsalud.security.monitor.ActividadSospechosaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Responde el error 401 (NO autenticado) con el formato JSON común (ErrorResponse).
 * Spring lo usa cuando falta el token, es inválido o el usuario ya no existe.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActividadSospechosaService actividadSospechosa;

    /**
     * Arma la respuesta 401. Si no vino token: "Se requiere un token de acceso";
     * si vino un token malo: usa el mensaje recibido (por ejemplo "Token inválido o expirado").
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        //Si llegó SIN token, lo anotamos en el log
        if (ex instanceof InsufficientAuthenticationException) {
            actividadSospechosa.registrarSinToken(request);
        }

        // Sin token, Spring manda un mensaje genérico en inglés: lo traducimos
        String mensaje = (ex instanceof InsufficientAuthenticationException)
                ? "Se requiere un token de acceso"
                : ex.getMessage();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
                ErrorResponse.of(HttpStatus.UNAUTHORIZED, mensaje, request.getRequestURI()));
    }
}