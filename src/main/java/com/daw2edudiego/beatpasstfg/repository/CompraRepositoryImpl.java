package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Compra;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional; // Importar Optional

/**
 * Implementación de CompraRepository usando JPA.
 * @author Eduardo Olalde
 */
public class CompraRepositoryImpl implements CompraRepository {

    private static final Logger log = LoggerFactory.getLogger(CompraRepositoryImpl.class);

    @Override
    public Compra save(EntityManager em, Compra compra) {
        if (compra == null || compra.getAsistente() == null) {
            throw new IllegalArgumentException("La entidad Compra y su Asistente asociado no pueden ser nulos.");
        }
        // Una compra siempre es nueva, usamos persist.
        log.debug("Intentando persistir nueva Compra para Asistente ID: {}", compra.getAsistente().getIdAsistente());
        try {
            em.persist(compra);
            em.flush(); // Para obtener el ID generado
            log.info("Nueva Compra persistida con ID: {}", compra.getIdCompra());
            return compra;
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Compra: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Compra: {}", e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Compra", e);
        }
    }

    @Override
    public Optional<Compra> findById(EntityManager em, Integer id) {
        log.debug("Buscando Compra con ID: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        try {
            // em.find es eficiente para buscar por PK
            return Optional.ofNullable(em.find(Compra.class, id));
        } catch (Exception e) {
            log.error("Error al buscar Compra por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }
}
