package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Consumo;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Consumo (consumo cashless).
 */
public interface ConsumoRepository {

    /**
     * Guarda un nuevo registro de consumo. Asume que no se actualizan. Debe
     * ejecutarse dentro de una transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param consumo La entidad Consumo a guardar.
     * @return La entidad Consumo guardada con su ID.
     */
    Consumo save(EntityManager em, Consumo consumo);

    /**
     * Busca un registro de consumo por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID del consumo a buscar.
     * @return Un Optional con el Consumo si se encuentra, o vacío.
     */
    Optional<Consumo> findById(EntityManager em, Integer id);

    /**
     * Busca todos los consumos asociados a una PulseraNFC específica.
     *
     * @param em El EntityManager activo.
     * @param idPulsera El ID de la PulseraNFC.
     * @return Una lista (posiblemente vacía) de Consumos.
     */
    List<Consumo> findByPulseraId(EntityManager em, Integer idPulsera);

    /**
     * Busca todos los consumos asociados a un Festival específico.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival.
     * @return Una lista (posiblemente vacía) de Consumos.
     */
    List<Consumo> findByFestivalId(EntityManager em, Integer idFestival);

}
