package com.petsalud.backendpetsalud.exception;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.petsalud.backendpetsalud.dto.ErrorResponse;
import com.petsalud.backendpetsalud.security.monitor.ActividadSospechosaService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Traductor central de errores: convierte las excepciones de controllers y services
 * en respuestas JSON con el código HTTP correcto (400, 401, 403, 409, 429, 500).
 * Spring elige el método cuyo tipo de excepción sea el MÁS específico, sin importar el orden.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private ActividadSospechosaService actividadSospechosa;

    // 409: email duplicado en el registro
    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ErrorResponse> emailDuplicado(EmailDuplicadoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // 429: demasiados intentos fallidos de login (cuenta bloqueada 15 minutos)
    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ErrorResponse> cuentaBloqueada(CuentaBloqueadaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req);
    }

    // 401: login con email o clave incorrectos (mensaje genérico a propósito)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> credenciales(BadCredentialsException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos", req);
    }

    // 403: @PreAuthorize rechazó el acceso (por rol o porque el recurso es de otra persona)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> sinPermiso(AccessDeniedException ex, HttpServletRequest req) {
        actividadSospechosa.registrarAccesoDenegado(req);
        return respuesta(HttpStatus.FORBIDDEN, "No tiene permisos para este recurso", req);
    }

    // 400: un @RequestBody con @Valid no cumplió las reglas
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacionBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, detalle, req);
    }

    // 400: un @RequestParam con anotaciones (@Pattern, @Min...) no cumplió las reglas
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> validacionParametros(HandlerMethodValidationException ex, HttpServletRequest req) {
        String detalle = ex.getAllValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(e -> r.getMethodParameter().getParameterName() + ": " + e.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, detalle, req);
    }

    // 400: JSON mal escrito, parámetro faltante o de tipo incorrecto
    @ExceptionHandler({HttpMessageNotReadableException.class,
                       MissingServletRequestParameterException.class,
                       MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> solicitudMalFormada(Exception ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud mal formada", req);
    }

    // 400: datos inválidos (por ejemplo, "Usuario no encontrado")
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(IllegalArgumentException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // 500: cualquier otro error. Se usa solo si ningún otro calza. Registra el detalle en el log, pero no lo muestra al cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInterno(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req);
    }

    /**
     * Arma la respuesta con el formato común ErrorResponse.
     */
    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, mensaje, req.getRequestURI()));
    }
}