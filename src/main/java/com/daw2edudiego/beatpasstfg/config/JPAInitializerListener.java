package com.daw2edudiego.beatpasstfg.config;

import com.daw2edudiego.beatpasstfg.util.JPAUtil;
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
        JPAUtil.shutdown(); // Cierra el EntityManagerFactory

        // Desregistrar manualmente el driver JDBC para prevenir memory leaks en Tomcat
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getName().equals("org.mariadb.jdbc.Driver")) {
                try {
                    DriverManager.deregisterDriver(driver);
                    log.info("JDBC Driver desregistrado: {}", driver);
                } catch (java.sql.SQLException e) {
                    log.error("Error al desregistrar JDBC Driver {}: {}", driver, e.getMessage(), e);
                }
            }
        }
        log.info("Contexto de la aplicación destruido.");
    }
}
