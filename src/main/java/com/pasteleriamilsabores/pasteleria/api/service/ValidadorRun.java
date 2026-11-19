package com.pasteleriamilsabores.pasteleria.api.service;


/**
 * Valida un RUN chileno con su dígito verificador (módulo 11).
 * Formato: sin puntos ni guion, por ejemplo 19011022K.
 * Es la misma regla de validaciones.js del frontend.
 */
public final class ValidadorRun {

    private ValidadorRun() {
    }

    public static boolean esValido(String run) {
        if (run == null) return false;
        String texto = run.trim().toUpperCase();
        if (texto.length() < 7 || texto.length() > 9) return false;

        String cuerpo = texto.substring(0, texto.length() - 1);
        char digitoIngresado = texto.charAt(texto.length() - 1);
        if (!cuerpo.matches("\\d+")) return false;

        // De derecha a izquierda, multiplicando por 2, 3, 4, 5, 6, 7, 2, 3...
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = 11 - (suma % 11);
        char digitoCalculado;
        if (resto == 11) digitoCalculado = '0';
        else if (resto == 10) digitoCalculado = 'K';
        else digitoCalculado = (char) ('0' + resto);

        return digitoCalculado == digitoIngresado;
    }
}