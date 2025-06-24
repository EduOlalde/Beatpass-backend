package com.beatpass.service;

import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.exception.UsuarioNotFoundException;
import com.beatpass.model.Festival;
import com.beatpass.model.RolUsuario;
import com.beatpass.model.Usuario;
import com.beatpass.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Clase base abstracta para servicios que necesitan gestión transaccional de
 * JPA. Proporciona métodos para ejecutar operaciones y lógica de negocio común,
 * como la verificación de permisos.
 */
public abstract class AbstractService {

    private static final Logger log = LoggerFactory.getLogger(AbstractService.class);

    /**
     * Ejecuta una operación de lectura dentro de un EntityManager. El
     * EntityManager se gestiona automáticamente.
     */
    protected <R> R executeRead(Function<EntityManager, R> operation, String operationName) {
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            log.debug("INICIO - Operación de lectura: {}", operationName);
            R result = operation.apply(em);
            log.debug("FIN - Operación de lectura '{}' completada.", operationName);
            return result;
        } catch (Exception e) {
            log.error("ERROR - durante la operación de lectura '{}': {}", operationName, e.getMessage(), e);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * Ejecuta una operación de escritura dentro de una transacción de JPA. El
     * EntityManager y la transacción se gestionan automáticamente.
     */
    protected <R> R executeTransactional(Function<EntityManager, R> operation, String operationName) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            log.info("INICIO TX - Operación transaccional: {}", operationName);
            R result = operation.apply(em);
            tx.commit();
            log.info("FIN TX - Operación transaccional '{}' completada (COMMIT).", operationName);
            return result;
        } catch (Exception e) {
            handleException(e, tx, operationName);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * Método centralizado de autorización. Verifica si un usuario (actor) tiene
     * permisos sobre un festival. El permiso se concede si el actor es ADMIN o
     * si es un PROMOTOR dueño del festival.
     *
     * @param em El EntityManager activo para realizar las consultas.
     * @param idFestival El ID del festival sobre el cual se verifica el
     * permiso.
     * @param idActor El ID del usuario que intenta realizar la acción.
     * @throws UsuarioNotFoundException Si el actor no se encuentra.
     * @throws FestivalNotFoundException Si el festival no se encuentra.
     * @throws SecurityException Si el actor no tiene los permisos requeridos.
     */
    protected void verificarPermisoSobreFestival(EntityManager em, Integer idFestival, Integer idActor) {
        if (idFestival == null) {
            throw new FestivalNotFoundException("El ID del festival no puede ser nulo para la verificación de permisos.");
        }
        if (idActor == null) {
            throw new IllegalArgumentException("El ID del usuario actor no puede ser nulo.");
        }

        // Buscar al actor que realiza la acción
        Usuario actor = em.find(Usuario.class, idActor);
        if (actor == null) {
            throw new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor);
        }

        // Un ADMIN siempre tiene permiso
        if (actor.getRol() == RolUsuario.ADMIN) {
            log.trace("Permiso concedido para festival ID {} a usuario ID {} (Rol: ADMIN).", idFestival, idActor);
            return;
        }

        // Buscar el festival
        Festival festival = em.find(Festival.class, idFestival);
        if (festival == null) {
            throw new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival);
        }

        // Si el actor es PROMOTOR, verificar que sea el dueño
        if (actor.getRol() == RolUsuario.PROMOTOR) {
            if (festival.getPromotor() != null && festival.getPromotor().getIdUsuario().equals(idActor)) {
                log.trace("Permiso concedido para festival ID {} a usuario promotor dueño ID {}.", idFestival, idActor);
                return;
            }
        }

        // Si no es ADMIN ni el PROMOTOR dueño, denegar acceso
        log.warn("Intento de acceso no autorizado por usuario ID {} (Rol: {}) al festival ID {} (Propiedad de Promotor ID {})",
                idActor,
                actor.getRol(),
                festival.getIdFestival(),
                festival.getPromotor() != null ? festival.getPromotor().getIdUsuario() : "N/A");
        throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
    }

    protected void handleException(Exception e, EntityTransaction tx, String operationName) {
        log.debug("Manejando excepción durante la acción '{}'. Intentando rollback.", operationName);
        rollbackTransaction(tx, operationName);
    }

    protected void rollbackTransaction(EntityTransaction tx, String operationName) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("ROLLBACK TX - Transacción de '{}' revertida.", operationName);
            } catch (Exception rbEx) {
                log.error("ERROR CRÍTICO - durante el rollback de la transacción '{}': {}", operationName, rbEx.getMessage(), rbEx);
            }
        }
    }

    protected void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            try {
                em.close();
                log.trace("EntityManager cerrado.");
            } catch (Exception e_close) {
                log.error("ERROR - al cerrar EntityManager: {}", e_close.getMessage(), e_close);
            }
        }
    }

    protected RuntimeException mapException(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio: " + e.getMessage(), e);
    }
}
