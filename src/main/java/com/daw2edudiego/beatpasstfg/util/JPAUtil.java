/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para gestionar el EntityManagerFactory de JPA. Sigue el
 * patrón Singleton para asegurar una única instancia del Factory.
 */
public class JPAUtil {

    // Nombre de la unidad de persistencia definida en persistence.xml
    private static final String PERSISTENCE_UNIT_NAME = "beatpassPersistenceUnit";
    private static EntityManagerFactory emf;
    private static final Logger log = LoggerFactory.getLogger(JPAUtil.class);

    // Inicialización estática para crear el EntityManagerFactory una sola vez
    static {
        try {
            log.info("Inicializando EntityManagerFactory para la unidad de persistencia: {}", PERSISTENCE_UNIT_NAME);
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            log.info("EntityManagerFactory inicializado correctamente.");
        } catch (Throwable ex) {
            // Loggear el error gravemente, ya que la aplicación no funcionará sin EMF
            log.error("Error GRAVE al inicializar EntityManagerFactory: " + ex.getMessage(), ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Obtiene la instancia única del EntityManagerFactory.
     *
     * @return El EntityManagerFactory configurado.
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            // Esto no debería ocurrir si la inicialización estática funcionó,
            // pero es una salvaguarda o indicativo de un problema mayor.
            log.error("EntityManagerFactory no está inicializado. Intentando reinicializar...");
            // Podrías intentar reinicializar aquí, pero es mejor investigar la causa raíz.
            throw new IllegalStateException("EntityManagerFactory no inicializado.");
        }
        return emf;
    }

    /**
     * Crea y devuelve un nuevo EntityManager. ¡IMPORTANTE! El código que llama
     * a este método es responsable de cerrar el EntityManager cuando ya no se
     * necesite (usualmente en un bloque finally).
     *
     * @return Un nuevo EntityManager.
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Cierra el EntityManagerFactory. Este método debería llamarse cuando la
     * aplicación se detiene (ej: en el método destroy() de un
     * ServletContextListener).
     */
    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            log.info("Cerrando EntityManagerFactory.");
            emf.close();
            log.info("EntityManagerFactory cerrado.");
        }
    }
}
