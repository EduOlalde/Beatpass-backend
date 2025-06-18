package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.TipoEntrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType; 
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad TipoEntrada (tipo de entrada).
 */
public interface TipoEntradaRepository {

    /**
     * Guarda (crea o actualiza) un TipoEntrada. Debe ejecutarse dentro de una
     * transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param tipoEntrada La entidad TipoEntrada a guardar.
     * @return La entidad TipoEntrada guardada o actualizada.
     */
    TipoEntrada save(EntityManager em, TipoEntrada tipoEntrada);

    /**
     * Busca un tipo de entrada por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID a buscar.
     * @return Un Optional con la TipoEntrada si se encuentra, o vacío.
     */
    Optional<TipoEntrada> findById(EntityManager em, Integer id);

    /**
     * Busca un tipo de entrada por su ID con un modo de bloqueo específico.
     *
     * @param em El EntityManager activo.
     * @param id El ID a buscar.
     * @param lockMode El modo de bloqueo a aplicar (ej:
     * LockModeType.PESSIMISTIC_WRITE).
     * @return Un Optional con la TipoEntrada si se encuentra, o vacío.
     */
    Optional<TipoEntrada> findById(EntityManager em, Integer id, LockModeType lockMode); // New method

    /**
     * Busca todos los tipos de entrada asociados a un Festival específico.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival.
     * @return Una lista (posiblemente vacía) de Entradas.
     */
    List<TipoEntrada> findByFestivalId(EntityManager em, Integer idFestival);

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
