package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.CompraEntrada;
import jakarta.persistence.EntityManager;
import java.util.List; // Importar List
import java.util.Optional; // Importar Optional

/**
 * Interfaz DAO para la entidad CompraEntrada (detalle de compra).
 * @author Eduardo Olalde
 */
public interface CompraEntradaRepository {

    /**
     * Guarda un nuevo detalle de compra. Asume transacción externa activa.
     *
     * @param em EntityManager activo.
     * @param compraEntrada El detalle a guardar (debe tener Compra y Entrada
     * asociadas).
     * @return El detalle guardado con su ID asignado.
     */
    CompraEntrada save(EntityManager em, CompraEntrada compraEntrada);

    /**
     * Busca un detalle de compra por su ID.
     *
     * @param em EntityManager activo.
     * @param id El ID del detalle de compra.
     * @return Optional con el detalle si se encuentra.
     */
    Optional<CompraEntrada> findById(EntityManager em, Integer id);

    /**
     * Busca todos los detalles asociados a una compra específica.
     *
     * @param em EntityManager activo.
     * @param idCompra El ID de la Compra.
     * @return Lista de detalles de esa compra.
     */
    List<CompraEntrada> findByCompraId(EntityManager em, Integer idCompra);

}
