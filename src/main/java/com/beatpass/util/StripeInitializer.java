package com.beatpass.util;

import com.stripe.Stripe;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inicializa la clave API de Stripe al arrancar la aplicación web. Lee la clave
 * desde la variable de entorno STRIPE_SECRET_KEY.
 */
@WebListener
public class StripeInitializer implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(StripeInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String stripeApiKey = System.getenv("STRIPE_SECRET_KEY");
        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            log.error("¡¡¡ERROR FATAL!!! La variable de entorno STRIPE_SECRET_KEY no está definida.");
            // Considerar lanzar excepción para detener el arranque si es crítico
        } else {
            Stripe.apiKey = stripeApiKey;
            log.info("Clave API de Stripe configurada correctamente.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Limpieza si fuera necesario (normalmente no se necesita para Stripe)
    }
}
