package com.beatpass.config;

import org.glassfish.jersey.server.ResourceConfig;
import com.beatpass.web.*;
import com.beatpass.mapper.GenericExceptionMapper;
import com.beatpass.security.AuthenticationFilter;
import com.beatpass.security.CorsFilter;

import jakarta.ws.rs.ApplicationPath;

/**
 * Clase de configuración principal para la aplicación JAX-RS, utilizando
 * ResourceConfig de Jersey para una configuración programática más flexible y
 * potente.
 * <p>
 * Define la ruta base para todos los endpoints de la API REST ({@code /api})
 * mediante la anotación {@link ApplicationPath}.
 * </p>
 *
 * @author Eduardo Olalde
 */
@ApplicationPath("/api")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        // Registrar Binder para la Inyección de Dependencias.
        register(new DependencyBinder());

        // Registrar clases de los recursos (endpoints)
        register(AdminResource.class);
        register(AuthResource.class);
        register(FestivalResource.class);
        register(PromotorResource.class);
        register(PublicVentaResource.class);
        register(PuntoVentaResource.class);
        register(UsuarioResource.class);

        // Registrar proveedores (filtros, mappers, etc.)
        register(AuthenticationFilter.class);
        register(CorsFilter.class);
        register(GenericExceptionMapper.class);
        register(ObjectMapperContextResolver.class);
    }
}
