package com.daw2edudiego.beatpasstfg.model;

/**
 * Enumeración que define los posibles estados de una {@link EntradaAsignada}.
 *
 * @author Eduardo Olalde
 */
public enum EstadoEntradaAsignada {
    /**
     * La entrada ha sido generada y/o nominada, está lista para ser usada o
     * asociada a una pulsera.
     */
    ACTIVA,
    /**
     * La entrada ya ha sido validada en el control de acceso o utilizada de
     * alguna forma.
     */
    USADA,
    /**
     * La entrada ha sido cancelada por un promotor o administrador y ya no es
     * válida.
     */
    CANCELADA
}
