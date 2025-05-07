package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Recarga;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Recarga.
 */
public interface RecargaRepository {

    /**
     * Guarda un nuevo registro de recarga. Asume que no se actualizan. Debe
     * ejecutarse dentro de una transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param recarga La entidad Recarga a guardar.
     * @return La entidad Recarga guardada con su ID.
     */
    Recarga save(EntityManager em, Recarga recarga);

    /**
     * Busca una recarga por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID a buscar.
     * @return Un Optional con la Recarga si se encuentra, o vacío.
     */
    Optional<Recarga> findById(EntityManager em, Integer id);

    /**
     * Busca todos los registros de recarga asociados a una PulseraNFC
     * específica.
     *
     * @param em El EntityManager activo.
     * @param idPulsera El ID de la PulseraNFC.
     * @return Una lista (posiblemente vacía) de Recargas.
     */
    List<Recarga> findByPulseraId(EntityManager em, Integer idPulsera);

}
