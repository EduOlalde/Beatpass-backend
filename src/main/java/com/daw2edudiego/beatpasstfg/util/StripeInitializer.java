package com.daw2edudiego.beatpasstfg.util;

import com.stripe.Stripe;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class StripeInitializer implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(StripeInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String stripeApiKey = System.getenv("STRIPE_SECRET_KEY");
        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            log.error("¡¡¡ERROR FATAL!!! La variable de entorno STRIPE_SECRET_KEY no está definida.");
            // Considera lanzar una excepción aquí para detener el arranque si es crítico
        } else {
            Stripe.apiKey = stripeApiKey;
            log.info("Clave API de Stripe configurada correctamente.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Limpieza si fuera necesario
    }
}