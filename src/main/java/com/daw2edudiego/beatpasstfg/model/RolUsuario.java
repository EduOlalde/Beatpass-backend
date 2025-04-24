package com.daw2edudiego.beatpasstfg.model;

/**
 * Enumeración que define los roles posibles para los usuarios del sistema
 * {@link Usuario}. Estos roles determinan los permisos y funcionalidades
 * accesibles. Coincide con los valores definidos en la columna `rol` de la
 * tabla `usuarios`.
 *
 * @author Eduardo Olalde
 */
public enum RolUsuario {
    /**
     * Rol de Administrador: Tiene acceso completo a la gestión de la
     * plataforma, incluyendo la gestión de promotores, festivales, asistentes,
     * etc.
     */
    ADMIN,
    /**
     * Rol de Promotor: Gestiona sus propios festivales, tipos de entrada,
     * nominación de entradas, visualización de asistentes y pulseras de sus
     * eventos.
     */
    PROMOTOR,
    /**
     * Rol de Cajero/Operador de Punto de Venta: Realiza operaciones con
     * pulseras NFC, como recargas de saldo y registro de consumos en puntos
     * físicos. (Este rol está en el script SQL pero no se mencionaba
     * explícitamente en la memoria inicial).
     */
    CAJERO
}
