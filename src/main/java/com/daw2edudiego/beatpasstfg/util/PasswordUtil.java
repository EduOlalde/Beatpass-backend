package com.daw2edudiego.beatpasstfg.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para manejar el hashing y la verificación de contraseñas
 * usando la librería jBCrypt.
 * <p>
 * BCrypt maneja automáticamente la generación de salt y su incrustación dentro
 * de la cadena hash.
 * </p>
 */
public class PasswordUtil {

    private static final Logger log = LoggerFactory.getLogger(PasswordUtil.class);

    /**
     * El factor de trabajo (work factor) para el hashing BCrypt. Valores más
     * altos incrementan la seguridad pero también el tiempo de computación. 12
     * se considera generalmente un buen equilibrio actualmente. Este valor
     * debería revisarse periódicamente y potencialmente incrementarse a medida
     * que el hardware mejora.
     */
    private static final int WORK_FACTOR = 12;

    /**
     * Genera un hash BCrypt para una contraseña en texto plano dada. Se genera
     * automáticamente un salt único y se incluye en la cadena hash resultante.
     *
     * @param plaintextPassword La contraseña a hashear. No puede ser nula ni
     * vacía.
     * @return La cadena hash BCrypt (incluyendo el salt).
     * @throws IllegalArgumentException si plaintextPassword es nula o vacía.
     */
    public static String hashPassword(String plaintextPassword) {
        if (plaintextPassword == null || plaintextPassword.isEmpty()) {
            log.warn("Intento de hashear una contraseña nula o vacía.");
            // Lanzar una excepción es más seguro que permitir contraseñas vacías.
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía.");
        }
        log.debug("Generando hash BCrypt con factor de trabajo: {}", WORK_FACTOR);
        // gensalt() genera un salt con las rondas logarítmicas especificadas (work factor)
        String salt = BCrypt.gensalt(WORK_FACTOR);
        // hashpw() hashea la contraseña usando el salt generado
        String hashedPassword = BCrypt.hashpw(plaintextPassword, salt);
        log.debug("Hash de contraseña generado exitosamente.");
        return hashedPassword;
    }

    /**
     * Verifica si una contraseña en texto plano dada coincide con un hash
     * BCrypt almacenado. El salt se extrae automáticamente de la cadena
     * hashedPassword por BCrypt.
     *
     * @param plaintextPassword La contraseña intentada (ej., introducida por el
     * usuario). No puede ser nula.
     * @param hashedPassword La cadena hash BCrypt almacenada (ej., de la base
     * de datos). No puede ser nula ni vacía.
     * @return {@code true} si la contraseña coincide con el hash, {@code false}
     * en caso contrario.
     */
    public static boolean checkPassword(String plaintextPassword, String hashedPassword) {
        log.debug("Verificando contraseña contra hash almacenado.");
        if (plaintextPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            log.warn("Intento de verificar contraseña con texto plano o hash nulos/vacíos.");
            return false;
        }

        boolean passwordsMatch = false;
        try {
            // checkpw compara la contraseña en texto plano contra el hash (que incluye el salt)
            passwordsMatch = BCrypt.checkpw(plaintextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Esta excepción puede ocurrir si el hash almacenado no tiene un formato BCrypt válido.
            log.error("Error verificando contraseña: El hash almacenado '{}' parece inválido. {}", hashedPassword, e.getMessage());
            // Devolver false ya que la verificación no pudo realizarse.
            return false;
        } catch (Exception e) {
            // Capturar errores inesperados durante checkpw
            log.error("Error inesperado durante la verificación de contraseña para el hash '{}': {}", hashedPassword, e.getMessage(), e);
            return false;
        }

        if (passwordsMatch) {
            log.debug("Verificación de contraseña exitosa.");
        } else {
            log.debug("Verificación de contraseña fallida (la contraseña no coincide con el hash).");
        }
        return passwordsMatch;
    }

    /**
     * Constructor privado para prevenir la instanciación.
     */
    private PasswordUtil() {
        // Clase de utilidad, no debe ser instanciada.
    }
}
