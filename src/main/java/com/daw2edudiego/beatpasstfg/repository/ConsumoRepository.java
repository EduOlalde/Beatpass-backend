package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Consumo;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Consumo.
 */
public interface ConsumoRepository {

    /**
     * Guarda un nuevo consumo. Asume transacción externa activa.
     *
     * @param em EntityManager activo.
     * @param consumo La entidad Consumo a guardar.
     * @return La entidad Consumo guardada.
     */
    Consumo save(EntityManager em, Consumo consumo);

    /**
     * Busca un consumo por su ID.
     *
     * @param em EntityManager activo.
     * @param id ID del consumo.
     * @return Optional con el consumo si se encuentra.
     */
    Optional<Consumo> findById(EntityManager em, Integer id);

    /**
     * Busca todos los consumos asociados a una pulsera específica.
     *
     * @param em EntityManager activo.
     * @param idPulsera ID de la PulseraNFC.
     * @return Lista de consumos de esa pulsera, ordenados por fecha
     * descendente.
     */
    List<Consumo> findByPulseraId(EntityManager em, Integer idPulsera);

    /**
     * Busca todos los consumos asociados a un festival específico.
     *
     * @param em EntityManager activo.
     * @param idFestival ID del Festival.
     * @return Lista de consumos de ese festival, ordenados por fecha
     * descendente.
     */
    List<Consumo> findByFestivalId(EntityManager em, Integer idFestival);

}
