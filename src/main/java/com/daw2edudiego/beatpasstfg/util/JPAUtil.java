// eduolalde/daw2-tfg-beatpass/DAW2-TFG-Beatpass-4c3b952517b6b4cf690ee7094dfc77d96c74f4c6/src/main/java/com/daw2edudiego/beatpasstfg/util/JPAUtil.java
package com.daw2edudiego.beatpasstfg.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "beatpassPersistenceUnit";
    private static EntityManagerFactory emf;
    private static final Logger log = LoggerFactory.getLogger(JPAUtil.class);

    // Método para inicializar el EMF
    public static void init() {
        if (emf == null) {
            log.info("Inicializando EntityManagerFactory para la unidad de persistencia: {}", PERSISTENCE_UNIT_NAME);
            try {
                Map<String, String> properties = new HashMap<>();
                String dbUrl = System.getenv("TFG_DB_URL");
                String dbUser = System.getenv("TFG_DB_USER");
                String dbPassword = System.getenv("TFG_DB_PASSWORD");

                if (dbUrl == null || dbUrl.trim().isEmpty() || dbUser == null || dbUser.trim().isEmpty() || dbPassword == null) {
                    throw new RuntimeException("Variables de entorno de base de datos (TFG_DB_URL, TFG_DB_USER, TFG_DB_PASSWORD) no están completamente definidas.");
                }

                properties.put("jakarta.persistence.jdbc.url", dbUrl);
                properties.put("jakarta.persistence.jdbc.user", dbUser);
                properties.put("jakarta.persistence.jdbc.password", dbPassword);

                Class.forName("com.mysql.cj.jdbc.Driver");

                emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
                log.info("EntityManagerFactory inicializado correctamente.");
            } catch (Exception e) {
                log.error("ERROR FATAL inicializando EntityManagerFactory: {}", e.getMessage(), e);
                throw new RuntimeException("No se pudo inicializar JPAUtil", e);
            }
        } else {
            log.warn("El EntityManagerFactory ya estaba inicializado.");
        }
    }

    // Método para cerrar el EMF, llamado por el Listener
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            log.info("Cerrando EntityManagerFactory.");
            emf.close();
            emf = null; 
            log.info("EntityManagerFactory cerrado correctamente.");
        }
    }

    public static EntityManager createEntityManager() {
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory no ha sido inicializado. Llama a JPAUtil.init() primero.");
        }
        return emf.createEntityManager();
    }
}
