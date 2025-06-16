package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Comprador;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CompradorRepositoryImpl implements CompradorRepository {

    private static final Logger log = LoggerFactory.getLogger(CompradorRepositoryImpl.class);

    @Override
    public Comprador save(EntityManager em, Comprador comprador) {
        if (comprador == null) {
            throw new IllegalArgumentException("La entidad Comprador no puede ser nula.");
        }
        log.debug("Intentando guardar Comprador con ID: {} y Email: {}", comprador.getIdComprador(), comprador.getEmail());
        try {
            if (comprador.getIdComprador() == null) {
                em.persist(comprador);
                log.info("Nuevo Comprador persistido con ID: {}", comprador.getIdComprador());
            } else {
                comprador = em.merge(comprador);
                log.info("Comprador actualizado con ID: {}", comprador.getIdComprador());
            }
            return comprador;
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Comprador (Email: {}): {}", comprador.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<Comprador> findById(EntityManager em, Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(em.find(Comprador.class, id));
        } catch (Exception e) {
            log.error("Error buscando Comprador por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Comprador> findByEmail(EntityManager em, String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            TypedQuery<Comprador> query = em.createQuery("SELECT c FROM Comprador c WHERE c.email = :email", Comprador.class);
            query.setParameter("email", email);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error buscando Comprador por email {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }
}
