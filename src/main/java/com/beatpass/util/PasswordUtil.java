package com.beatpass.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para hashing y verificación de contraseñas usando jBCrypt.
 */
public class PasswordUtil {

    private static final Logger log = LoggerFactory.getLogger(PasswordUtil.class);
    private static final int WORK_FACTOR = 12; // Factor de trabajo para BCrypt

    /**
     * Genera un hash BCrypt para una contraseña dada.
     *
     * @param plaintextPassword La contraseña a hashear (no nula/vacía).
     * @return La cadena hash BCrypt (incluyendo salt).
     * @throws IllegalArgumentException si plaintextPassword es nula o vacía.
     */
    public static String hashPassword(String plaintextPassword) {
        if (plaintextPassword == null || plaintextPassword.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía.");
        }
        log.debug("Generando hash BCrypt con factor de trabajo: {}", WORK_FACTOR);
        String salt = BCrypt.gensalt(WORK_FACTOR);
        String hashedPassword = BCrypt.hashpw(plaintextPassword, salt);
        log.debug("Hash de contraseña generado exitosamente.");
        return hashedPassword;
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt
     * almacenado.
     *
     * @param plaintextPassword La contraseña intentada (no nula).
     * @param hashedPassword La cadena hash BCrypt almacenada (no nula/vacía).
     * @return true si la contraseña coincide, false en caso contrario o si el
     * hash es inválido.
     */
    public static boolean checkPassword(String plaintextPassword, String hashedPassword) {
        log.debug("Verificando contraseña contra hash almacenado.");
        if (plaintextPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            log.warn("Intento de verificar contraseña con texto plano o hash nulos/vacíos.");
            return false;
        }

        boolean passwordsMatch = false;
        try {
            passwordsMatch = BCrypt.checkpw(plaintextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            log.error("Error verificando contraseña: El hash almacenado parece inválido. Hash='{}', Error: {}", hashedPassword, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error inesperado durante la verificación de contraseña para el hash '{}': {}", hashedPassword, e.getMessage(), e);
            return false;
        }

        if (passwordsMatch) {
            log.debug("Verificación de contraseña exitosa.");
        } else {
            log.debug("Verificación de contraseña fallida.");
        }
        return passwordsMatch;
    }

    // Prevenir instanciación
    private PasswordUtil() {
    }
}
