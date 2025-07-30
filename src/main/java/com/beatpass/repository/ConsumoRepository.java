package com.beatpass.repository;

import com.beatpass.model.Consumo;
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
     * @param consumo La entidad Consumo a guardar.
     * @return La entidad Consumo guardada con su ID.
     */
    Consumo save(Consumo consumo);

    /**
     * Busca un registro de consumo por su ID.
     *
     * @param id El ID del consumo a buscar.
     * @return Un Optional con el Consumo si se encuentra, o vacío.
     */
    Optional<Consumo> findById(Integer id);

    /**
     * Busca todos los consumos asociados a una PulseraNFC específica.
     *
     * @param idPulsera El ID de la PulseraNFC.
     * @return Una lista (posiblemente vacía) de Consumos.
     */
    List<Consumo> findByPulseraId(Integer idPulsera);

    /**
     * Busca todos los consumos asociados a un Festival específico.
     *
     * @param idFestival El ID del Festival.
     * @return Una lista (posiblemente vacía) de Consumos.
     */
    List<Consumo> findByFestivalId(Integer idFestival);

}
