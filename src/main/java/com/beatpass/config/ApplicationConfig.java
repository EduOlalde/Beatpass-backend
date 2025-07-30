package com.beatpass.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Clase de configuración principal para la aplicación JAX-RS.
 * <p>
 * Define la ruta base para todos los endpoints de la API REST ({@code /api})
 * mediante la anotación {@link ApplicationPath}. Al extender {@link Application},
 * WildFly escaneará y registrará automáticamente todos los recursos JAX-RS
 * (clases con @Path) y proveedores (clases con @Provider).
 * </p>
 *
 * @author Eduardo Olalde
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
        info = @Info(
                title = "Beatpass API",
                version = "1.0.0",
                description = "API RESTful para la gestión de venta de entradas para festivales."
        ),
        servers = {
            @Server(url = "https://beatpass.onrender.com", description = "Servidor de Producción"),
            @Server(url = "http://localhost:8080", description = "Servidor Local")
        }
)
public class ApplicationConfig extends Application {

}