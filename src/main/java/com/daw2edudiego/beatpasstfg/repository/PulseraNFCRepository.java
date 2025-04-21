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
     * Guarda (crea o actualiza) una pulsera NFC. Asume transacción externa
     * activa.
     *
     * @param em EntityManager activo.
     * @param pulsera La entidad PulseraNFC a guardar.
     * @return La entidad guardada/actualizada.
     */
    PulseraNFC save(EntityManager em, PulseraNFC pulsera);

    /**
     * Busca una pulsera por su ID de base de datos.
     *
     * @param em EntityManager activo.
     * @param id El ID de la pulsera.
     * @return Optional con la pulsera si se encuentra.
     */
    Optional<PulseraNFC> findById(EntityManager em, Integer id);

    /**
     * Busca una pulsera por su código UID (que es único).
     *
     * @param em EntityManager activo.
     * @param codigoUid El código UID a buscar.
     * @return Optional con la pulsera si se encuentra.
     */
    Optional<PulseraNFC> findByCodigoUid(EntityManager em, String codigoUid);

    /**
     * Busca la pulsera asociada a una EntradaAsignada específica. Debería haber
     * como máximo una.
     *
     * @param em EntityManager activo.
     * @param idEntradaAsignada ID de la EntradaAsignada.
     * @return Optional con la pulsera si se encuentra asociada.
     */
    Optional<PulseraNFC> findByEntradaAsignadaId(EntityManager em, Integer idEntradaAsignada);

    /**
     * Busca todas las pulseras asociadas a un festival (a través de
     * EntradaAsignada).
     *
     * @param em EntityManager activo.
     * @param idFestival ID del festival.
     * @return Lista de pulseras asociadas a ese festival.
     */
    List<PulseraNFC> findByFestivalId(EntityManager em, Integer idFestival);

}
