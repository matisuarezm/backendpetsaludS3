package com.petsalud.backendpetsalud.validation;

/**
 * Expresiones regulares reutilizables para @Pattern.
 * Funcionan como "listas blancas": definen qué caracteres SÍ se aceptan y rechazan el resto
 * (por ejemplo < > ' " ; usados en ataques XSS e inyección SQL).
 */
public final class Patrones {

    private Patrones() {}

    // Solo letras (incluye tildes y ñ), separadas por un espacio o guion: "María José", "Pastor-Alemán"
    public static final String NOMBRE = "^[\\p{L}]+([ \\-][\\p{L}]+)*$";

    // Texto libre, pero SIN caracteres usados en ataques: < > " ' ` ; \  y sin "--" (comentario SQL)
    public static final String TEXTO_SEGURO = "^(?!.*--)[^<>\"'`;\\\\]*$";

    // Teléfono chileno: opcional "+" y 8 a 12 dígitos
    public static final String TELEFONO = "^\\+?[0-9]{8,12}$";

    // Contraseña: 8 a 72 caracteres, al menos una letra y un número, sin espacios
    public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)\\S{8,72}$";
}