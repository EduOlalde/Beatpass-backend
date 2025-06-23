package com.beatpass.service;

import com.beatpass.exception.StockInsuficienteException;
import com.beatpass.exception.PagoInvalidoException;
import com.beatpass.exception.TipoEntradaNotFoundException;
import com.beatpass.exception.FestivalNoPublicadoException;
import com.beatpass.dto.CompraDTO;
import com.beatpass.dto.IniciarCompraResponseDTO;

/**
 * Define la lógica de negocio para el proceso de venta de entradas. Orquesta
 * validaciones, stock, generación de entradas y confirmación de pago.
 */
public interface VentaService {

    /**
     * Confirma una venta después de verificar un pago exitoso con Stripe. Crea
     * o recupera al comprador, crea la compra, genera entradas y actualiza
     * stock. Es transaccional.
     *
     * @param emailComprador Email del comprador (obligatorio).
     * @param nombreComprador Nombre del comprador (obligatorio).
     * @param telefonoComprador Teléfono del comprador (opcional).
     * @param idTipoEntrada ID del tipo de entrada.
     * @param cantidad Número de entradas (> 0).
     * @param paymentIntentId ID del PaymentIntent de Stripe ('pi_...').
     * @return DTO de la Compra creada.
     * @throws EntradaNotFoundException, FestivalNoPublicadoException,
     * StockInsuficienteException, PagoInvalidoException,
     * IllegalArgumentException.
     */
    CompraDTO confirmarVentaConPago(String emailComprador, String nombreComprador, String telefonoComprador, Integer idTipoEntrada, int cantidad, String paymentIntentId)
            throws TipoEntradaNotFoundException, FestivalNoPublicadoException,
            StockInsuficienteException, PagoInvalidoException, IllegalArgumentException;

    /**
     * Inicia el proceso de pago creando un PaymentIntent en Stripe. Calcula el
     * total y devuelve el client_secret para el frontend. No modifica la BD.
     *
     * @param idTipoEntrada ID del tipo de entrada deseado.
     * @param cantidad Número de entradas deseadas (> 0).
     * @return DTO con el client_secret de Stripe.
     * @throws TipoEntradaNotFoundException si la entrada no existe.
     * @throws FestivalNoPublicadoException si el festival no está publicado.
     * @throws IllegalArgumentException si los datos son inválidos.
     * @throws RuntimeException si ocurre un error con Stripe.
     */
    IniciarCompraResponseDTO iniciarProcesoPago(Integer idTipoEntrada, int cantidad)
            throws TipoEntradaNotFoundException, FestivalNoPublicadoException, IllegalArgumentException;

}
