package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Asistente;
import jakarta.persistence.EntityManager;

import java.util.Optional;

/**
 * Interfaz DAO para la entidad Asistente.
 * @author Eduardo Olalde
 */
public interface AsistenteRepository {

    /**
     * Guarda (crea o actualiza) un asistente. Asume transacción externa activa.
     *
     * @param em EntityManager activo.
     * @param asistente El asistente a guardar.
     * @return El asistente guardado/actualizado.
     */
    Asistente save(EntityManager em, Asistente asistente);

    /**
     * Busca un asistente por su ID.
     *
     * @param em EntityManager activo.
     * @param id ID del asistente.
     * @return Optional con el asistente si se encuentra.
     */
    Optional<Asistente> findById(EntityManager em, Integer id);

    /**
     * Busca un asistente por su email (que es único).
     *
     * @param em EntityManager activo.
     * @param email Email del asistente.
     * @return Optional con el asistente si se encuentra.
     */
    Optional<Asistente> findByEmail(EntityManager em, String email);

    // Se podrían añadir más métodos si fueran necesarios (ej: findAll)
}
