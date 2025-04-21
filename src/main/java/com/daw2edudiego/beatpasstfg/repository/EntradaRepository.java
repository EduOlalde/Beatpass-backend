package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Entrada;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define el contrato para las operaciones de acceso a datos (DAO)
 * relacionadas con la entidad Entrada (tipos de entrada).
 * @author Eduardo Olalde
 */
public interface EntradaRepository {

    /**
     * Guarda (crea o actualiza) un tipo de entrada. Asume transacción externa
     * activa.
     *
     * @param em EntityManager activo.
     * @param entrada La entidad Entrada a guardar.
     * @return La entidad Entrada guardada/actualizada.
     */
    Entrada save(EntityManager em, Entrada entrada);

    /**
     * Busca un tipo de entrada por su ID.
     *
     * @param em EntityManager activo.
     * @param id El ID del tipo de entrada.
     * @return Optional con la entidad si se encuentra.
     */
    Optional<Entrada> findById(EntityManager em, Integer id);

    /**
     * Busca todos los tipos de entrada asociados a un festival específico.
     *
     * @param em EntityManager activo.
     * @param idFestival El ID del festival.
     * @return Lista de entidades Entrada para ese festival.
     */
    List<Entrada> findByFestivalId(EntityManager em, Integer idFestival);

    /**
     * Elimina un tipo de entrada por su ID. Asume transacción externa activa.
     *
     * @param em EntityManager activo.
     * @param id El ID del tipo de entrada a eliminar.
     * @return true si se encontró y marcó para eliminar, false si no. Puede
     * lanzar PersistenceException si hay restricciones FK.
     */
    boolean deleteById(EntityManager em, Integer id);

}
