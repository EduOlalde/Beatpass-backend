package com.daw2edudiego.beatpasstfg.util;

// Imports cambiados a javax.persistence.*
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para gestionar el EntityManagerFactory de JPA. Sigue el
 * patrón Singleton para asegurar una única instancia del Factory. ¡CORREGIDO
 * para usar javax.persistence!
 */
public class JPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "beatpassPersistenceUnit";
    private static EntityManagerFactory emf;
    private static final Logger log = LoggerFactory.getLogger(JPAUtil.class);

    static {
        try {
            log.info("Inicializando EntityManagerFactory para la unidad de persistencia: {}", PERSISTENCE_UNIT_NAME);
            // Usar javax.persistence.Persistence
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            log.info("EntityManagerFactory inicializado correctamente.");
        } catch (Throwable ex) {
            // La excepción raíz podría ser ahora javax.persistence.PersistenceException
            log.error("Error GRAVE al inicializar EntityManagerFactory: " + ex.getMessage(), ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            log.error("EntityManagerFactory no está inicializado.");
            throw new IllegalStateException("EntityManagerFactory no inicializado.");
        }
        return emf;
    }

    public static EntityManager createEntityManager() {
        // Devuelve javax.persistence.EntityManager
        return getEntityManagerFactory().createEntityManager();
    }

    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            log.info("Cerrando EntityManagerFactory.");
            emf.close();
            log.info("EntityManagerFactory cerrado.");
        }
    }
}
