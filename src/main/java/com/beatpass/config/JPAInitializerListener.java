// src/main/java/com/daw2edudiego/beatpasstfg/config/JPAInitializerListener.java
package com.beatpass.config;

import com.beatpass.util.JPAUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.DriverManager;
import java.sql.Driver;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class JPAInitializerListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(JPAInitializerListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Iniciando contexto de la aplicación. Inicializando JPA.");
        JPAUtil.init();
        log.info("JPA inicializado correctamente.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Destruyendo contexto de la aplicación. Cerrando JPA y desregistrando drivers.");
        JPAUtil.shutdown();

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        Driver mariadbDriver = null;
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getName().equals("org.mariadb.jdbc.Driver") && driver.getClass().getClassLoader() == this.getClass().getClassLoader()) {
                mariadbDriver = driver;
                break;
            } else if (driver.getClass().getName().equals("org.mariadb.jdbc.Driver")) {
                mariadbDriver = driver;
            }
        }

        if (mariadbDriver != null) {
            try {
                DriverManager.deregisterDriver(mariadbDriver);
                log.info("JDBC Driver {} desregistrado explícitamente.", mariadbDriver.getClass().getName());
            } catch (java.sql.SQLException e) {
                log.error("Error al desregistrar JDBC Driver {}: {}", mariadbDriver.getClass().getName(), e.getMessage(), e);
            }
        } else {
            log.warn("JDBC Driver org.mariadb.jdbc.Driver no encontrado en DriverManager para desregistrar.");
        }
        log.info("Contexto de la aplicación destruido.");
    }
}
