package com.beatpass.repository;

import com.beatpass.model.CompraEntrada;
import jakarta.persistence.EntityManager;
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
     * @param em El EntityManager activo y transaccional.
     * @param compraEntrada El detalle de compra a guardar.
     * @return El detalle de compra guardado con su ID.
     */
    CompraEntrada save(EntityManager em, CompraEntrada compraEntrada);

    /**
     * Busca un detalle de compra por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID del detalle de compra a buscar.
     * @return Un Optional con el CompraEntrada si se encuentra, o vacío.
     */
    Optional<CompraEntrada> findById(EntityManager em, Integer id);

    /**
     * Busca todos los detalles asociados a una Compra específica.
     *
     * @param em El EntityManager activo.
     * @param idCompra El ID de la Compra.
     * @return Una lista (posiblemente vacía) de CompraEntrada.
     */
    List<CompraEntrada> findByCompraId(EntityManager em, Integer idCompra);

}
