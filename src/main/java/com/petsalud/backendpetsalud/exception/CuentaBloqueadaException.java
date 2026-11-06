package com.petsalud.backendpetsalud.exception;

/** Se lanza cuando alguien intenta hacer login estando bloqueado por demasiados intentos fallidos. */
public class CuentaBloqueadaException extends RuntimeException {
    public CuentaBloqueadaException() {
        super("Demasiados intentos fallidos. Intente nuevamente en 15 minutos.");
    }
}