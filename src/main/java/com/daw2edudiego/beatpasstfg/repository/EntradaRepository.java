package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Entrada;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Entrada (tipo de entrada).
 */
public interface EntradaRepository {

    /**
     * Guarda (crea o actualiza) una Entrada. Debe ejecutarse dentro de una
     * transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param entrada La entidad Entrada a guardar.
     * @return La entidad Entrada guardada o actualizada.
     */
    Entrada save(EntityManager em, Entrada entrada);

    /**
     * Busca un tipo de entrada por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID a buscar.
     * @return Un Optional con la Entrada si se encuentra, o vacío.
     */
    Optional<Entrada> findById(EntityManager em, Integer id);

    /**
     * Busca todos los tipos de entrada asociados a un Festival específico.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival.
     * @return Una lista (posiblemente vacía) de Entradas.
     */
    List<Entrada> findByFestivalId(EntityManager em, Integer idFestival);

    /**
     * Elimina un tipo de entrada por su ID. Debe ejecutarse dentro de una
     * transacción activa. ¡Precaución con FKs!
     *
     * @param em El EntityManager activo y transaccional.
     * @param id El ID a eliminar.
     * @return true si se encontró y marcó para eliminar, false si no se
     * encontró.
     */
    boolean deleteById(EntityManager em, Integer id);

}
