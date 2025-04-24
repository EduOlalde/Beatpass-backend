package com.daw2edudiego.beatpasstfg.service;

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
     */
    void registrarVenta(Integer idAsistente, Integer idEntrada, int cantidad);

    // Aquí podrían añadirse otros métodos relacionados con el ciclo de vida de una venta,
    // como consulta de historial de compras por asistente, gestión de devoluciones (si aplica), etc.
    // Ejemplo:
    // List<CompraResumenDTO> obtenerHistorialCompras(Integer idAsistente);
    // void solicitarDevolucion(Integer idCompra, Integer idUsuario);
}
