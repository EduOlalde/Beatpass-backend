package com.daw2edudiego.beatpasstfg.model;

/**
 * Enumeración que define los posibles estados de un {@link Festival}.
 *
 * @author Eduardo Olalde
 */
public enum EstadoFestival {
    /**
     * El festival está siendo creado o modificado por el promotor, aún no es
     * visible públicamente ni se pueden vender entradas.
     */
    BORRADOR,
    /**
     * El festival ha sido aprobado/publicado por un administrador y es visible,
     * permitiendo la venta de entradas.
     */
    PUBLICADO,
    /**
     * El festival ha sido cancelado (antes o durante su celebración).
     */
    CANCELADO,
    /**
     * El festival ha concluido.
     */
    FINALIZADO
}
