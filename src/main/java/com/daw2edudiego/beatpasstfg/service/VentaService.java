package com.daw2edudiego.beatpasstfg.service;

// Podríamos devolver un DTO con resumen de la venta si fuera necesario
// import com.daw2edudiego.beatpasstfg.dto.VentaResumenDTO;
import com.daw2edudiego.beatpasstfg.exception.*; // Importar todas las excepciones

/**
 * Interfaz que define las operaciones de negocio relacionadas con el proceso de
 * venta de entradas.
 */
public interface VentaService {

    /**
     * Registra una nueva venta de entradas para un asistente y un tipo de
     * entrada específicos. Esta operación debe ser transaccional. - Verifica la
     * existencia del asistente y del tipo de entrada. - Verifica que el
     * festival asociado a la entrada esté PUBLICADO. - Verifica que haya stock
     * suficiente para la cantidad solicitada. - Crea los registros de Compra y
     * CompraEntrada. - Genera las EntradasAsignadas correspondientes, cada una
     * con un código QR único. - Decrementa el stock del tipo de entrada.
     *
     * @param idAsistente ID del asistente que realiza la compra.
     * @param idEntrada ID del tipo de entrada que se compra.
     * @param cantidad Número de entradas a comprar.
     * @throws AsistenteNotFoundException si el asistente no existe.
     * @throws EntradaNotFoundException si el tipo de entrada no existe.
     * @throws FestivalNoPublicadoException si el festival no está publicado.
     * @throws StockInsuficienteException si no hay suficiente stock.
     * @throws IllegalArgumentException si la cantidad es inválida (<= 0).
     * @throws RuntimeException si ocurre un error inesperado durante la
     * transacción.
     */
    void registrarVenta(Integer idAsistente, Integer idEntrada, int cantidad);

    // Podrían añadirse otros métodos relacionados con ventas, devoluciones, etc.
}
