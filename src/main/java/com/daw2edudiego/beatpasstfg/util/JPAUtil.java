package com.daw2edudiego.beatpasstfg.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para gestionar el {@link EntityManagerFactory} de JPA.
 * <p>
 * Sigue el patrón Singleton para asegurar que solo se cree una instancia del
 * EntityManagerFactory para la unidad de persistencia especificada. Proporciona
 * métodos para obtener la factoría y crear instancias de {@link EntityManager}.
 *
 * @author Eduardo Olalde
 * </p>
 */
public class JPAUtil {

    // El nombre de la unidad de persistencia definida en persistence.xml
    private static final String PERSISTENCE_UNIT_NAME = "beatpassPersistenceUnit";
    private static EntityManagerFactory emf;
    private static final Logger log = LoggerFactory.getLogger(JPAUtil.class);

    // Bloque inicializador estático para crear el EntityManagerFactory cuando se carga la clase.
    static {
        try {
            log.info("Inicializando EntityManagerFactory para la unidad de persistencia: {}", PERSISTENCE_UNIT_NAME);
            // Crear el EntityManagerFactory usando el nombre de la unidad de persistencia
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            log.info("EntityManagerFactory inicializado correctamente.");
        } catch (PersistenceException ex) { // Capturar PersistenceException específica
            // Registrar la causa raíz si está disponible
            if (ex.getCause() != null) {
                log.error("Causa raíz del error de inicialización de EntityManagerFactory: ", ex.getCause());
            }
            log.error("ERROR FATAL inicializando EntityManagerFactory: {}", ex.getMessage(), ex);
            // Lanzar ExceptionInInitializerError señala un fallo en la inicialización estática
            throw new ExceptionInInitializerError(ex);
        } catch (Throwable ex) { // Capturar cualquier otro error inesperado
            log.error("ERROR FATAL inesperado durante la inicialización de EntityManagerFactory: {}", ex.getMessage(), ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Devuelve la instancia singleton de {@link EntityManagerFactory}.
     *
     * @return El EntityManagerFactory inicializado.
     * @throws IllegalStateException si el EntityManagerFactory no se inicializó
     * correctamente.
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            // Esto idealmente no debería ocurrir debido al inicializador estático,
            // pero proporciona una salvaguarda.
            log.error("EntityManagerFactory no está inicializado. Revisa los logs de inicialización.");
            throw new IllegalStateException("EntityManagerFactory no ha sido inicializado.");
        }
        return emf;
    }

    /**
     * Crea una nueva instancia de {@link EntityManager} desde la factoría.
     * <p>
     * Recuerda cerrar el EntityManager después de usarlo, típicamente en un
     * bloque finally o usando try-with-resources si es aplicable (aunque
     * EntityManager no es AutoCloseable).
     * </p>
     *
     * @return Una nueva instancia de EntityManager.
     */
    public static EntityManager createEntityManager() {
        log.debug("Creando nueva instancia de EntityManager.");
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Cierra la instancia singleton de {@link EntityManagerFactory}. Este
     * método solo debería llamarse cuando la aplicación se está cerrando para
     * liberar recursos.
     */
    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            log.info("Cerrando EntityManagerFactory.");
            emf.close();
            log.info("EntityManagerFactory cerrado.");
        } else {
            log.info("EntityManagerFactory ya estaba cerrado o nunca fue inicializado.");
        }
    }

    /**
     * Constructor privado para prevenir la instanciación.
     */
    private JPAUtil() {
        // Clase de utilidad, no debe ser instanciada.
    }
}
