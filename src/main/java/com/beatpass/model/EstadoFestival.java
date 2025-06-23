package com.beatpass.model;

/**
 * Estados posibles de un Festival.
 */
public enum EstadoFestival {
    /**
     * El festival está en creación/modificación, no visible ni vendible.
     */
    BORRADOR,
    /**
     * El festival está aprobado, visible y se pueden vender entradas.
     */
    PUBLICADO,
    /**
     * El festival ha sido cancelado.
     */
    CANCELADO,
    /**
     * El festival ha concluido.
     */
    FINALIZADO
}
