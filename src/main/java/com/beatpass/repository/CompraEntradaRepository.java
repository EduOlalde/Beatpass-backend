package com.beatpass.repository;

import com.beatpass.model.CompraEntrada;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad CompraEntrada (detalle de compra).
 */
public interface CompraEntradaRepository {

    /**
     * Guarda un nuevo detalle de compra. Asume que no se actualizan. Debe
     * ejecutarse dentro de una transacción activa.
     *
     * @param compraEntrada El detalle de compra a guardar.
     * @return El detalle de compra guardado con su ID.
     */
    CompraEntrada save(CompraEntrada compraEntrada);

    /**
     * Busca un detalle de compra por su ID.
     *
     * @param id El ID del detalle de compra a buscar.
     * @return Un Optional con el CompraEntrada si se encuentra, o vacío.
     */
    Optional<CompraEntrada> findById(Integer id);

    /**
     * Busca todos los detalles asociados a una Compra específica.
     *
     * @param idCompra El ID de la Compra.
     * @return Una lista (posiblemente vacía) de CompraEntrada.
     */
    List<CompraEntrada> findByCompraId(Integer idCompra);

}
