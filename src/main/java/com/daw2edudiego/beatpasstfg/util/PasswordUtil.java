/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilidad para manejar el hashing y verificación de contraseñas usando
 * jBCrypt.
 */
public class PasswordUtil {

    private static final Logger log = LoggerFactory.getLogger(PasswordUtil.class);
    // El "work factor" de BCrypt. Valores más altos son más seguros pero más lentos.
    // 12 es un buen punto de partida actualmente.
    private static final int WORK_FACTOR = 12;

    /**
     * Genera el hash de una contraseña usando BCrypt.
     *
     * @param passwordPlano La contraseña en texto plano.
     * @return El hash de la contraseña (incluye el salt).
     */
    public static String hashPassword(String passwordPlano) {
        if (passwordPlano == null || passwordPlano.isEmpty()) {
            log.warn("Intento de hashear una contraseña vacía o nula.");
            // Decide cómo manejar esto: lanzar excepción o devolver null/vacío?
            // Lanzar excepción suele ser más seguro para evitar contraseñas vacías.
            throw new IllegalArgumentException("La contraseña no puede ser vacía.");
        }
        log.debug("Generando hash para contraseña.");
        String salt = BCrypt.gensalt(WORK_FACTOR);
        String hashedPassword = BCrypt.hashpw(passwordPlano, salt);
        log.debug("Hash generado exitosamente.");
        return hashedPassword;
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash existente.
     *
     * @param passwordPlano La contraseña en texto plano introducida por el
     * usuario.
     * @param hashedPassword El hash almacenado en la base de datos.
     * @return true si las contraseñas coinciden, false en caso contrario.
     */
    public static boolean checkPassword(String passwordPlano, String hashedPassword) {
        log.debug("Verificando contraseña.");
        if (passwordPlano == null || hashedPassword == null || hashedPassword.isEmpty()) {
            log.warn("Intento de verificar contraseña con datos nulos o vacíos.");
            return false;
        }
        boolean match = false;
        try {
            match = BCrypt.checkpw(passwordPlano, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Esto puede ocurrir si el hash almacenado no es un hash BCrypt válido
            log.error("Error al verificar contraseña: el hash almacenado parece inválido. Hash: {}", hashedPassword, e);
            return false;
        }
        if (match) {
            log.debug("La contraseña coincide.");
        } else {
            log.debug("La contraseña NO coincide.");
        }
        return match;
    }
}
