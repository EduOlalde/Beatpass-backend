package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Comprador;
import jakarta.persistence.EntityManager;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Comprador.
 */
public interface CompradorRepository {

    Comprador save(EntityManager em, Comprador comprador);

    Optional<Comprador> findById(EntityManager em, Integer id);

    Optional<Comprador> findByEmail(EntityManager em, String email);
}
