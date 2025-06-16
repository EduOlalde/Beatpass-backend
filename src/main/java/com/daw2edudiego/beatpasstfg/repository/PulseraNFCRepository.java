package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.PulseraNFC;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad PulseraNFC.
 */
public interface PulseraNFCRepository {

    /**
     * Guarda (crea o actualiza) una PulseraNFC. Debe ejecutarse dentro de una
     * transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param pulsera La entidad PulseraNFC a guardar.
     * @return La entidad PulseraNFC guardada o actualizada.
     */
    PulseraNFC save(EntityManager em, PulseraNFC pulsera);

    /**
     * Busca una PulseraNFC por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID a buscar.
     * @return Un Optional con la PulseraNFC si se encuentra, o vacío.
     */
    Optional<PulseraNFC> findById(EntityManager em, Integer id);

    /**
     * Busca una PulseraNFC por su código UID (único).
     *
     * @param em El EntityManager activo.
     * @param codigoUid El código UID a buscar.
     * @return Un Optional con la PulseraNFC si se encuentra, o vacío.
     */
    Optional<PulseraNFC> findByCodigoUid(EntityManager em, String codigoUid);

    /**
     * Busca la PulseraNFC asociada a una Entrada específica.
     *
     * @param em El EntityManager activo.
     * @param idEntrada El ID de la Entrada.
     * @return Un Optional con la PulseraNFC si se encuentra, o vacío.
     */
    Optional<PulseraNFC> findByEntradaId(EntityManager em, Integer idEntrada);

    /**
     * Busca todas las pulseras asociadas a un Festival específico.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival.
     * @return Una lista (posiblemente vacía) de PulseraNFC.
     */
    List<PulseraNFC> findByFestivalId(EntityManager em, Integer idFestival);

}
