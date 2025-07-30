package com.beatpass.repository;

import com.beatpass.model.Comprador;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ApplicationScoped
public class CompradorRepositoryImpl implements CompradorRepository {

    @PersistenceContext(unitName = "beatpassPersistenceUnit")
    private EntityManager em;
    private static final Logger log = LoggerFactory.getLogger(CompradorRepositoryImpl.class);

    @Override
    public Comprador save(Comprador comprador) {
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
    public Optional<Comprador> findById(Integer id) {
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
    public Optional<Comprador> findByEmail(String email) {
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

    @Override
    public List<Comprador> searchByTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return em.createQuery("SELECT c FROM Comprador c ORDER BY c.nombre", Comprador.class)
                    .getResultList();
        }

        TypedQuery<Comprador> query = em.createQuery(
                "SELECT c FROM Comprador c WHERE lower(c.nombre) LIKE :term OR lower(c.email) LIKE :term ORDER BY c.nombre", Comprador.class);
        query.setParameter("term", "%" + searchTerm.toLowerCase() + "%");
        return query.getResultList();
    }
}
