// src/main/java/com/daw2edudiego/beatpasstfg/config/JPAInitializerListener.java
package com.daw2edudiego.beatpasstfg.config;

import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class JPAInitializerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Se ejecuta una sola vez cuando la aplicación arranca
        JPAUtil.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Se ejecuta cuando la aplicación se detiene o se recarga
        JPAUtil.shutdown();
    }
}
