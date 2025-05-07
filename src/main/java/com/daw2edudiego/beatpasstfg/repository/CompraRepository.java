package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Compra;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Compra (cabecera de compra).
 */
public interface CompraRepository {

    /**
     * Guarda una nueva Compra. Asume que no se actualizan. Debe ejecutarse
     * dentro de una transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param compra La Compra a guardar.
     * @return La Compra guardada con su ID.
     */
    Compra save(EntityManager em, Compra compra);

    /**
     * Busca una Compra por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID de la compra a buscar.
     * @return Un Optional con la Compra si se encuentra, o vacío.
     */
    Optional<Compra> findById(EntityManager em, Integer id);

    /**
     * Busca todas las compras realizadas por un Asistente específico.
     *
     * @param em El EntityManager activo.
     * @param idAsistente El ID del Asistente.
     * @return Una lista (posiblemente vacía) de Compras.
     */
    List<Compra> findByAsistenteId(EntityManager em, Integer idAsistente);

    /**
     * Busca todas las compras asociadas a un Festival específico.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival.
     * @return Una lista (posiblemente vacía) de Compras únicas.
     */
    List<Compra> findByFestivalId(EntityManager em, Integer idFestival);

}
