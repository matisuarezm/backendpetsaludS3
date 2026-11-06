package com.petsalud.backendpetsalud.dto;

import java.time.Instant;
import org.springframework.http.HttpStatus;

/**
 * Formato único de TODAS las respuestas de error: fecha, código, nombre del error, mensaje y ruta.
 * Es un "record": una clase solo de datos (Java genera constructor y getters).
 */
public record ErrorResponse(String timestamp, int status, String error, String message, String path) {

    /**
     * Atajo para crear el error con la fecha actual a partir del código HTTP, el mensaje y la ruta.
     */
    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(Instant.now().toString(), status.value(),
                status.getReasonPhrase(), message, path);
    }
}