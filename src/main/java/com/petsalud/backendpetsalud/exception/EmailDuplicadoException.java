package com.petsalud.backendpetsalud.exception;

/**
 * Error propio para "email ya registrado".
 * Tener un nombre propio permite responder 409 (conflicto) sin confundirlo con otros errores.
 */
public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException(String email) {
        super("El email " + email + " ya está registrado");
    }
}