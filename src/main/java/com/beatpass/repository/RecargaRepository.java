package com.beatpass.repository;

import com.beatpass.model.Recarga;
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
     * @param recarga La entidad Recarga a guardar.
     * @return La entidad Recarga guardada con su ID.
     */
    Recarga save(Recarga recarga);

    /**
     * Busca una recarga por su ID.
     *
     * @param id El ID a buscar.
     * @return Un Optional con la Recarga si se encuentra, o vacío.
     */
    Optional<Recarga> findById(Integer id);

    /**
     * Busca todos los registros de recarga asociados a una PulseraNFC
     * específica.
     *
     * @param idPulsera El ID de la PulseraNFC.
     * @return Una lista (posiblemente vacía) de Recargas.
     */
    List<Recarga> findByPulseraId(Integer idPulsera);

}
