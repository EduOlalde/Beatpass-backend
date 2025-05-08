package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraResponseDTO;
import com.daw2edudiego.beatpasstfg.exception.*;

/**
 * Define la lógica de negocio para el proceso de venta de entradas. Orquesta
 * validaciones, stock, generación de entradas y confirmación de pago.
 */
public interface VentaService {

    /**
     * Confirma una venta después de verificar un pago exitoso con Stripe.
     * Realiza validaciones, crea la compra, genera entradas y actualiza stock.
     * Es transaccional.
     *
     * @param idAsistente ID del comprador.
     * @param idEntrada ID del tipo de entrada.
     * @param cantidad Número de entradas (> 0).
     * @param paymentIntentId ID del PaymentIntent de Stripe ('pi_...') que debe
     * estar 'succeeded'.
     * @return DTO de la Compra creada.
     * @throws AsistenteNotFoundException, EntradaNotFoundException,
     * FestivalNoPublicadoException, StockInsuficienteException,
     * PagoInvalidoException, IllegalArgumentException.
     */
    CompraDTO confirmarVentaConPago(Integer idAsistente, Integer idEntrada, int cantidad, String paymentIntentId)
            throws AsistenteNotFoundException, EntradaNotFoundException, FestivalNoPublicadoException,
            StockInsuficienteException, PagoInvalidoException, IllegalArgumentException;

    /**
     * Inicia el proceso de pago creando un PaymentIntent en Stripe. Calcula el
     * total y devuelve el client_secret para el frontend. No modifica la BD.
     *
     * @param idEntrada ID del tipo de entrada deseado.
     * @param cantidad Número de entradas deseadas (> 0).
     * @return DTO con el client_secret de Stripe.
     * @throws EntradaNotFoundException si la entrada no existe.
     * @throws FestivalNoPublicadoException si el festival no está publicado.
     * @throws IllegalArgumentException si los datos son inválidos.
     * @throws RuntimeException si ocurre un error con Stripe.
     */
    IniciarCompraResponseDTO iniciarProcesoPago(Integer idEntrada, int cantidad)
            throws EntradaNotFoundException, FestivalNoPublicadoException, IllegalArgumentException;

}
