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
     * Guarda una nueva recarga. Asume transacción externa activa.
     *
     * @param em EntityManager activo.
     * @param recarga La entidad Recarga a guardar.
     * @return La entidad Recarga guardada.
     */
    Recarga save(EntityManager em, Recarga recarga);

    /**
     * Busca una recarga por su ID.
     *
     * @param em EntityManager activo.
     * @param id ID de la recarga.
     * @return Optional con la recarga si se encuentra.
     */
    Optional<Recarga> findById(EntityManager em, Integer id);

    /**
     * Busca todas las recargas asociadas a una pulsera específica.
     *
     * @param em EntityManager activo.
     * @param idPulsera ID de la PulseraNFC.
     * @return Lista de recargas de esa pulsera, ordenadas por fecha
     * descendente.
     */
    List<Recarga> findByPulseraId(EntityManager em, Integer idPulsera);

}
