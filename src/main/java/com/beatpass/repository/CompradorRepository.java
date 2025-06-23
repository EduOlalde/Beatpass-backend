package com.beatpass.repository;

import com.beatpass.model.Comprador;
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
