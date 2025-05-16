package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO; // Ya deberías tener este DTO
import java.util.List;

/**
 * Define la lógica de negocio para el envío de correos electrónicos.
 */
public interface EmailService {

    /**
     * Envía un correo electrónico de confirmación al comprador después de una
     * compra exitosa. Incluye detalles básicos de las entradas no nominadas y
     * el PDF adjunto.
     *
     * @param destinatarioEmail Email del comprador.
     * @param nombreComprador Nombre del comprador.
     * @param nombreFestival Nombre del festival.
     * @param entradasCompradas Lista de DTOs de las entradas asignadas (aún sin
     * nominar o con datos básicos).
     */
    void enviarEmailEntradasCompradas(String destinatarioEmail, String nombreComprador, String nombreFestival, List<EntradaAsignadaDTO> entradasCompradas);

    /**
     * Envía un correo electrónico al asistente nominado con los detalles de su
     * entrada. Incluye el PDF de la entrada nominada.
     *
     * @param destinatarioEmail Email del asistente nominado.
     * @param nombreNominado Nombre del asistente nominado.
     * @param entradaNominada DTO de la entrada asignada ya nominada.
     */
    void enviarEmailEntradaNominada(String destinatarioEmail, String nombreNominado, EntradaAsignadaDTO entradaNominada);
}
