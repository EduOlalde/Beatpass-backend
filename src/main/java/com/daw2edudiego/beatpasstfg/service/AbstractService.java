package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Clase base abstracta para servicios que necesitan gestión transaccional de
 * JPA. Proporciona métodos para ejecutar operaciones dentro de una transacción
 * y manejar el EntityManager.
 */
public abstract class AbstractService {

    private static final Logger log = LoggerFactory.getLogger(AbstractService.class);

    /**
     * Ejecuta una operación de lectura dentro de un EntityManager. El
     * EntityManager se abrirá y cerrará automáticamente. No se gestiona ninguna
     * transacción, ya que es para operaciones de solo lectura.
     *
     * @param <R> Tipo de retorno de la operación.
     * @param operation La función que contiene la lógica de negocio. Recibe un
     * EntityManager.
     * @param operationName Nombre de la operación para fines de logging.
     * @return El resultado de la operación.
     * @throws RuntimeException Si ocurre algún error durante la operación o la
     * gestión del EntityManager.
     */
    protected <R> R executeRead(Function<EntityManager, R> operation, String operationName) {
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            log.debug("Ejecutando operación de lectura: {}", operationName);
            return operation.apply(em);
        } catch (Exception e) {
            log.error("Error durante la operación de lectura '{}': {}", operationName, e.getMessage(), e);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * Ejecuta una operación de escritura dentro de una transacción de JPA. El
     * EntityManager y la transacción se gestionan automáticamente (begin,
     * commit/rollback, close).
     *
     * @param <R> Tipo de retorno de la operación.
     * @param operation La función que contiene la lógica de negocio. Recibe un
     * EntityManager.
     * @param operationName Nombre de la operación para fines de logging.
     * @return El resultado de la operación.
     * @throws RuntimeException Si ocurre algún error durante la operación o la
     * gestión de la transacción.
     */
    protected <R> R executeTransactional(Function<EntityManager, R> operation, String operationName) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            log.debug("Iniciando transacción para operación de escritura: {}", operationName);
            R result = operation.apply(em);
            tx.commit();
            log.info("Transacción de escritura '{}' completada (commit).", operationName);
            return result;
        } catch (Exception e) {
            handleException(e, tx, operationName);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * Maneja excepciones, realizando un rollback si la transacción está activa
     * y mapeando la excepción a un tipo RuntimeException adecuado.
     *
     * @param e La excepción original.
     * @param tx La transacción actual (puede ser nula).
     * @param operationName El nombre de la operación en la que ocurrió el
     * error.
     */
    protected void handleException(Exception e, EntityTransaction tx, String operationName) {
        log.error("Error durante la acción '{}': {}", operationName, e.getMessage(), e);
        rollbackTransaction(tx, operationName);
    }

    /**
     * Realiza un rollback de la transacción si está activa.
     *
     * @param tx La transacción a la que se le hará rollback.
     * @param operationName El contexto de la operación para el log.
     */
    protected void rollbackTransaction(EntityTransaction tx, String operationName) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("Rollback de transacción de '{}' realizado.", operationName);
            } catch (Exception rbEx) {
                log.error("Error crítico durante el rollback de la transacción '{}': {}", operationName, rbEx.getMessage(), rbEx);
            }
        }
    }

    /**
     * Cierra el EntityManager si está abierto.
     *
     * @param em El EntityManager a cerrar.
     */
    protected void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            try {
                em.close();
            } catch (Exception e_close) {
                log.error("Error al cerrar EntityManager: {}", e_close.getMessage(), e_close);
            }
        }
    }

    /**
     * Mapea una excepción genérica a una RuntimeException específica de la capa
     * de servicio, o la envuelve en una RuntimeException si no es una excepción
     * de negocio conocida.
     *
     * @param e La excepción a mapear.
     * @return Una RuntimeException adecuada.
     */
    protected RuntimeException mapException(Exception e) {
        // Aquí puedes añadir lógica para mapear excepciones JPA específicas
        // a tus excepciones de negocio personalizadas si aún no lo has hecho.
        // Por ejemplo:
        if (e instanceof PersistenceException) {
            // Podrías inspeccionar la causa raíz para lanzar algo más específico
            // if (e.getCause() instanceof ConstraintViolationException) { ... }
            return new RuntimeException("Error de persistencia: " + e.getMessage(), e);
        }
        // Si ya es una RuntimeException (incluyendo tus excepciones personalizadas como NotFound, etc.)
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        // Para cualquier otra excepción inesperada
        return new RuntimeException("Error inesperado en la capa de servicio: " + e.getMessage(), e);
    }
}
