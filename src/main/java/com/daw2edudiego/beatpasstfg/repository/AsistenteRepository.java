package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Asistente;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Asistente. Define las operaciones de
 * persistencia estándar.
 */
public interface AsistenteRepository {

    /**
     * Guarda (crea o actualiza) una entidad Asistente. Debe ejecutarse dentro
     * de una transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param asistente La entidad Asistente a guardar.
     * @return La entidad Asistente guardada o actualizada.
     */
    Asistente save(EntityManager em, Asistente asistente);

    /**
     * Busca un Asistente por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID del asistente a buscar.
     * @return Un Optional con el Asistente si se encuentra, o vacío.
     */
    Optional<Asistente> findById(EntityManager em, Integer id);

    /**
     * Busca un Asistente por su email (único).
     *
     * @param em El EntityManager activo.
     * @param email El email del asistente a buscar.
     * @return Un Optional con el Asistente si se encuentra, o vacío.
     */
    Optional<Asistente> findByEmail(EntityManager em, String email);

    /**
     * Busca asistentes únicos que tienen entradas para un festival específico.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del festival.
     * @return Una lista (posiblemente vacía) de Asistentes únicos.
     */
    List<Asistente> findAsistentesByFestivalId(EntityManager em, Integer idFestival);

    /**
     * Busca todos los asistentes. Usar con precaución.
     *
     * @param em El EntityManager activo.
     * @return Una lista con todos los asistentes.
     */
    List<Asistente> findAll(EntityManager em);

    /**
     * Elimina un asistente. Debe ejecutarse dentro de una transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param asistente El asistente a eliminar.
     */
    void delete(EntityManager em, Asistente asistente);

}
