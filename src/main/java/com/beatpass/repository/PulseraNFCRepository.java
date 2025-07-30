package com.beatpass.repository;

import com.beatpass.model.PulseraNFC;
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
     * @param pulsera La entidad PulseraNFC a guardar.
     * @return La entidad PulseraNFC guardada o actualizada.
     */
    PulseraNFC save(PulseraNFC pulsera);

    /**
     * Busca una PulseraNFC por su ID.
     *
     * @param id El ID a buscar.
     * @return Un Optional con la PulseraNFC si se encuentra, o vacío.
     */
    Optional<PulseraNFC> findById(Integer id);

    /**
     * Busca una PulseraNFC por su código UID (único).
     *
     * @param codigoUid El código UID a buscar.
     * @return Un Optional con la PulseraNFC si se encuentra, o vacío.
     */
    Optional<PulseraNFC> findByCodigoUid(String codigoUid);

    /**
     * Busca la PulseraNFC asociada a una Entrada específica.
     *
     * @param idEntrada El ID de la Entrada.
     * @return Un Optional con la PulseraNFC si se encuentra, o vacío.
     */
    Optional<PulseraNFC> findByEntradaId(Integer idEntrada);

    /**
     * Busca todas las pulseras asociadas a un Festival específico.
     *
     * @param idFestival El ID del Festival.
     * @return Una lista (posiblemente vacía) de PulseraNFC.
     */
    List<PulseraNFC> findByFestivalId(Integer idFestival);

}
