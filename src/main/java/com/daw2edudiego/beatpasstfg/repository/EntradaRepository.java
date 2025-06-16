package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Entrada;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Entrada (entrada individual).
 */
public interface EntradaRepository {

    /**
     * Guarda (crea o actualiza) una Entrada. Debe ejecutarse dentro de
     * una transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param entrada La entidad a guardar.
     * @return La entidad guardada o actualizada.
     */
    Entrada save(EntityManager em, Entrada entrada);

    /**
     * Busca una Entrada por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID a buscar.
     * @return Un Optional con la Entrada si se encuentra, o vacío.
     */
    Optional<Entrada> findById(EntityManager em, Integer id);

    /**
     * Busca una Entrada por su código QR (único).
     *
     * @param em El EntityManager activo.
     * @param codigoQr El código QR a buscar.
     * @return Un Optional con la Entrada si se encuentra, o vacío.
     */
    Optional<Entrada> findByCodigoQr(EntityManager em, String codigoQr);

    /**
     * Busca todas las entradas generadas desde un CompraEntrada
     * específico.
     *
     * @param em El EntityManager activo.
     * @param idCompraEntrada El ID del CompraEntrada origen.
     * @return Una lista (posiblemente vacía) de Entrada.
     */
    List<Entrada> findByCompraEntradaId(EntityManager em, Integer idCompraEntrada);

    /**
     * Busca todas las entradas que pertenecen a un Festival
     * específico.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival.
     * @return Una lista (posiblemente vacía) de Entrada.
     */
    List<Entrada> findByFestivalId(EntityManager em, Integer idFestival);

}
