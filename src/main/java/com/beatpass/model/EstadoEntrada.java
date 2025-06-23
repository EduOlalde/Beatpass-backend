package com.beatpass.model;

/**
 * Estados posibles de una EntradaAsignada.
 */
public enum EstadoEntrada {
    /**
     * La entrada ha sido generada y/o nominada, lista para usarse.
     */
    ACTIVA,
    /**
     * La entrada ya ha sido validada o utilizada.
     */
    USADA,
    /**
     * La entrada ha sido cancelada y ya no es válida.
     */
    CANCELADA
}
