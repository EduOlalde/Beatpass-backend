package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraResponseDTO;
import com.daw2edudiego.beatpasstfg.exception.*;

/**
 * Define la interfaz para la lógica de negocio relacionada con el proceso de
 * venta de entradas. Esta capa orquesta las operaciones necesarias para
 * registrar una venta, asegurando la validez de los datos, la disponibilidad de
 * stock, la generación de entradas individuales y la actualización del
 * inventario.
 *
 * @see VentaServiceImpl
 * @author Eduardo Olalde
 */
public interface VentaService {

    /**
     * Registra una nueva venta de entradas para un asistente y un tipo de
     * entrada específicos.
     * <p>
     * Esta operación es <b>transaccional</b> y realiza las siguientes acciones:
     * <ol>
     * <li>Verifica la existencia del asistente ({@code idAsistente}) y del tipo
     * de entrada ({@code idEntrada}).</li>
     * <li>Verifica que el festival asociado al tipo de entrada esté en estado
     * {@link com.daw2edudiego.beatpasstfg.model.EstadoFestival#PUBLICADO}.</li>
     * <li>Verifica que haya stock suficiente del tipo de entrada para la
     * {@code cantidad} solicitada (usando bloqueo pesimista para evitar
     * condiciones de carrera).</li>
     * <li>Crea un registro {@link com.daw2edudiego.beatpasstfg.model.Compra}
     * para el asistente.</li>
     * <li>Crea un registro
     * {@link com.daw2edudiego.beatpasstfg.model.CompraEntrada} detallando el
     * tipo de entrada, cantidad y precio unitario en el momento de la
     * compra.</li>
     * <li>Genera la {@code cantidad} correspondiente de entidades
     * {@link com.daw2edudiego.beatpasstfg.model.EntradaAsignada}, cada una con
     * un código QR único y estado inicial ACTIVA.</li>
     * <li>Decrementa el stock del
     * {@link com.daw2edudiego.beatpasstfg.model.Entrada} original.</li>
     * </ol>
     * </p>
     *
     * @param idAsistente ID del asistente que realiza la compra. No debe ser
     * {@code null}.
     * @param idEntrada ID del tipo de entrada que se compra. No debe ser
     * {@code null}.
     * @param cantidad Número de entradas a comprar. Debe ser un entero
     * estrictamente positivo.
     * @throws AsistenteNotFoundException Si el asistente con
     * {@code idAsistente} no existe.
     * @throws EntradaNotFoundException Si el tipo de entrada con
     * {@code idEntrada} no existe.
     * @throws FestivalNoPublicadoException Si el festival asociado a la entrada
     * no está en estado PUBLICADO.
     * @throws StockInsuficienteException Si el stock disponible para el tipo de
     * entrada es menor que la {@code cantidad} solicitada.
     * @throws IllegalArgumentException Si {@code idAsistente},
     * {@code idEntrada} son {@code null} o si {@code cantidad} es menor o igual
     * a cero.
     * @throws RuntimeException Si ocurre un error inesperado durante la
     * transacción (ej: error al generar QR, error de persistencia).
     * @deprecated Considerar usar
     * {@link #confirmarVentaConPago(Integer, Integer, int, String)}
     */
    void registrarVenta(Integer idAsistente, Integer idEntrada, int cantidad);

    /**
     * Confirma una venta después de verificar un pago exitoso con Stripe.
     *
     * @param idAsistente ID del asistente que realiza la compra.
     * @param idEntrada ID del tipo de entrada que se compra.
     * @param cantidad Número de entradas a comprar (debe ser > 0).
     * @param paymentIntentId El ID del PaymentIntent de Stripe (ej: "pi_...")
     * que ya debe estar en estado 'succeeded'.
     * @return Un DTO de la Compra creada.
     * @throws AsistenteNotFoundException Si el asistente no existe.
     * @throws EntradaNotFoundException Si la entrada no existe.
     * @throws FestivalNoPublicadoException Si el festival no está publicado.
     * @throws StockInsuficienteException Si no hay stock suficiente.
     * @throws PagoInvalidoException Si el PaymentIntent no es válido o no
     * coincide.
     * @throws IllegalArgumentException Si los IDs son nulos, cantidad <= 0, o
     * paymentIntentId es inválido. @throws RuntimeException Si ocurre un error
     * inesperado.
     */
    CompraDTO confirmarVentaConPago(Integer idAsistente, Integer idEntrada, int cantidad, String paymentIntentId)
            throws AsistenteNotFoundException, EntradaNotFoundException, FestivalNoPublicadoException,
            StockInsuficienteException, PagoInvalidoException, IllegalArgumentException;

    /**
     * Inicia el proceso de pago para una compra potencial. Calcula el total
     * basado en el tipo de entrada y la cantidad, crea un PaymentIntent en
     * Stripe y devuelve su client_secret. Este método NO crea registros de
     * Compra ni descuenta stock; eso ocurre en {@link #confirmarVentaConPago}.
     *
     * @param idEntrada ID del tipo de entrada deseado. No debe ser
     * {@code null}.
     * @param cantidad Número de entradas deseadas. Debe ser > 0.
     * @return Un {@link IniciarCompraResponseDTO} que contiene el client_secret
     * del PaymentIntent creado.
     * @throws EntradaNotFoundException Si el tipo de entrada no existe.
     * @throws FestivalNoPublicadoException Si el festival asociado no está
     * publicado.
     * @throws IllegalArgumentException Si idEntrada es null o cantidad <= 0.
     * @throws RuntimeException Si ocurre un error al calcular el total o al
     * interactuar con Stripe.
     */
    IniciarCompraResponseDTO iniciarProcesoPago(Integer idEntrada, int cantidad)
            throws EntradaNotFoundException, FestivalNoPublicadoException, IllegalArgumentException;

}
