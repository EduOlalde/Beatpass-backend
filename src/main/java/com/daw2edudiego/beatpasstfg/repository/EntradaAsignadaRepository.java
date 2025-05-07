package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.EntradaAsignada;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad EntradaAsignada (entrada individual).
 */
public interface EntradaAsignadaRepository {

    /**
     * Guarda (crea o actualiza) una EntradaAsignada. Debe ejecutarse dentro de
     * una transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param entradaAsignada La entidad a guardar.
     * @return La entidad guardada o actualizada.
     */
    EntradaAsignada save(EntityManager em, EntradaAsignada entradaAsignada);

    /**
     * Busca una EntradaAsignada por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID a buscar.
     * @return Un Optional con la EntradaAsignada si se encuentra, o vacío.
     */
    Optional<EntradaAsignada> findById(EntityManager em, Integer id);

    /**
     * Busca una EntradaAsignada por su código QR (único).
     *
     * @param em El EntityManager activo.
     * @param codigoQr El código QR a buscar.
     * @return Un Optional con la EntradaAsignada si se encuentra, o vacío.
     */
    Optional<EntradaAsignada> findByCodigoQr(EntityManager em, String codigoQr);

    /**
     * Busca todas las entradas asignadas generadas desde un CompraEntrada
     * específico.
     *
     * @param em El EntityManager activo.
     * @param idCompraEntrada El ID del CompraEntrada origen.
     * @return Una lista (posiblemente vacía) de EntradaAsignada.
     */
    List<EntradaAsignada> findByCompraEntradaId(EntityManager em, Integer idCompraEntrada);

    /**
     * Busca todas las entradas asignadas que pertenecen a un Festival
     * específico.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival.
     * @return Una lista (posiblemente vacía) de EntradaAsignada.
     */
    List<EntradaAsignada> findByFestivalId(EntityManager em, Integer idFestival);

}
