package com.daw2edudiego.beatpasstfg.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase de utilidad Singleton para gestionar el EntityManagerFactory de JPA.
 * Lee la configuración de BD desde variables de entorno.
 */
public class JPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "beatpassPersistenceUnit";
    private static EntityManagerFactory emf;
    private static final Logger log = LoggerFactory.getLogger(JPAUtil.class);

    static {
        try {
            log.info("Inicializando EntityManagerFactory para la unidad de persistencia: {}", PERSISTENCE_UNIT_NAME);

            Map<String, String> properties = new HashMap<>();

            String dbUrl = System.getenv("TFG_DB_URL");
            String dbUser = System.getenv("TFG_DB_USER");
            String dbPassword = System.getenv("TFG_DB_PASSWORD");

            if (dbUrl == null || dbUrl.trim().isEmpty()) {
                throw new RuntimeException("Variable de entorno TFG_DB_URL no definida o vacía.");
            }
            if (dbUser == null || dbUser.trim().isEmpty()) {
                throw new RuntimeException("Variable de entorno TFG_DB_USER no definida o vacía.");
            }
            if (dbPassword == null) {
                log.warn("Variable de entorno TFG_DB_PASSWORD no definida. Se usará una cadena vacía.");
                dbPassword = "";
            }

            log.info("Usando DB_URL obtenida de variable de entorno."); // No loguear URL
            log.info("Usando DB_USER obtenido de variable de entorno: {}", dbUser);
            log.info("Usando DB_PASSWORD obtenida de variable de entorno."); // No loguear PASS

            properties.put("jakarta.persistence.jdbc.url", dbUrl);
            properties.put("jakarta.persistence.jdbc.user", dbUser);
            properties.put("jakarta.persistence.jdbc.password", dbPassword);

            try {
                // Asegurar que el driver esté disponible
                Class.forName("com.mysql.cj.jdbc.Driver");
                log.info("Driver MySQL cargado explícitamente.");
            } catch (ClassNotFoundException e) {
                log.error("¡ERROR FATAL! No se encontró el driver JDBC de MySQL en el classpath.", e);
                throw new RuntimeException("Driver MySQL no encontrado", e);
            }

            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
            log.info("EntityManagerFactory inicializado correctamente usando variables de entorno.");

        } catch (PersistenceException ex) {
            log.error("ERROR FATAL inicializando EntityManagerFactory: {}", ex.getMessage(), ex.getCause() != null ? ex.getCause() : ex);
            throw new ExceptionInInitializerError(ex);
        } catch (Throwable ex) {
            log.error("ERROR FATAL inesperado durante la inicialización de EntityManagerFactory: {}", ex.getMessage(), ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Devuelve la instancia singleton de EntityManagerFactory.
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            // Esto no debería ocurrir si el bloque static funcionó, pero como salvaguarda
            log.error("EntityManagerFactory no está inicializado. Revisa los logs de inicialización.");
            throw new IllegalStateException("EntityManagerFactory no ha sido inicializado.");
        }
        return emf;
    }

    /**
     * Crea una nueva instancia de EntityManager. Recordar cerrarlo después de
     * usar.
     */
    public static EntityManager createEntityManager() {
        log.debug("Creando nueva instancia de EntityManager.");
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Cierra la instancia singleton de EntityManagerFactory.
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

    // Prevenir instanciación
    private JPAUtil() {
    }
}
