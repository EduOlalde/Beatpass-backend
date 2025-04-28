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
 * Clase de utilidad para gestionar el {@link EntityManagerFactory} de JPA.
 * <p>
 * Sigue el patrón Singleton para asegurar que solo se cree una instancia del
 * EntityManagerFactory. Lee la configuración de la base de datos (URL, usuario,
 * contraseña) desde variables de entorno (DB_URL, DB_USER, DB_PASSWORD).
 * </p>
 *
 * @author Eduardo Olalde
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

            // Crear un mapa para pasar propiedades programáticamente
            Map<String, String> properties = new HashMap<>();

            // Leer variables de entorno
            String dbUrl = System.getenv("TFG_DB_URL");
            String dbUser = System.getenv("TFG_DB_USER");
            String dbPassword = System.getenv("TFG_DB_PASSWORD");

            // Verificar si las variables de entorno existen
            if (dbUrl == null || dbUrl.trim().isEmpty()) {
                log.error("¡Variable de entorno DB_URL no definida o vacía!");
                throw new RuntimeException("Variable de entorno DB_URL no definida o vacía.");
            }
            if (dbUser == null || dbUser.trim().isEmpty()) {
                log.error("¡Variable de entorno DB_USER no definida o vacía!");
                throw new RuntimeException("Variable de entorno DB_USER no definida o vacía.");
            }
            if (dbPassword == null) {
                // Permitir contraseña vacía, pero loguear si no está definida
                log.warn("Variable de entorno DB_PASSWORD no definida. Se usará una cadena vacía si es necesario.");
                dbPassword = ""; // O manejar como error si siempre se requiere contraseña
            }

            log.info("Usando DB_URL obtenida de variable de entorno."); // No loguear la URL completa por seguridad
            log.info("Usando DB_USER obtenido de variable de entorno: {}", dbUser);
            log.info("Usando DB_PASSWORD obtenida de variable de entorno."); // NUNCA loguear la contraseña

            // Añadir las propiedades leídas al mapa
            properties.put("jakarta.persistence.jdbc.url", dbUrl);
            properties.put("jakarta.persistence.jdbc.user", dbUser);
            properties.put("jakarta.persistence.jdbc.password", dbPassword);

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                log.info("Driver MySQL cargado explícitamente.");
            } catch (ClassNotFoundException e) {
                log.error("¡ERROR FATAL! No se encontró el driver JDBC de MySQL en el classpath.", e);
                throw new RuntimeException("Driver MySQL no encontrado", e);
            }

            // 5. Crear el EntityManagerFactory pasando el mapa de propiedades
            // Las propiedades en el mapa sobrescriben o complementan las de persistence.xml
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);

            log.info("EntityManagerFactory inicializado correctamente usando variables de entorno.");

        } catch (PersistenceException ex) { // Capturar PersistenceException específica
            if (ex.getCause() != null) {
                log.error("Causa raíz del error de inicialización de EntityManagerFactory: ", ex.getCause());
            }
            log.error("ERROR FATAL inicializando EntityManagerFactory: {}", ex.getMessage(), ex);
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
            log.error("EntityManagerFactory no está inicializado. Revisa los logs de inicialización.");
            throw new IllegalStateException("EntityManagerFactory no ha sido inicializado.");
        }
        return emf;
    }

    /**
     * Crea una nueva instancia de {@link EntityManager} desde la factoría.
     * <p>
     * Recuerda cerrar el EntityManager después de usarlo.
     * </p>
     *
     * @return Una nueva instancia de EntityManager.
     */
    public static EntityManager createEntityManager() {
        log.debug("Creando nueva instancia de EntityManager.");
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Cierra la instancia singleton de {@link EntityManagerFactory}.
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
