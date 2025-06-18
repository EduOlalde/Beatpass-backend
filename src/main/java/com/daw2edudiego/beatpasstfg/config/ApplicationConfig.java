package com.daw2edudiego.beatpasstfg.config;

import com.daw2edudiego.beatpasstfg.mapper.GenericExceptionMapper;
import com.daw2edudiego.beatpasstfg.security.AuthenticationFilter;
import com.daw2edudiego.beatpasstfg.web.*;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase de configuración principal para la aplicación JAX-RS.
 * <p>
 * Define la ruta base para todos los endpoints de la API REST ({@code /api})
 * mediante la anotación {@link ApplicationPath}. Sobrescribe el método
 * {@link #getClasses()} para registrar explícitamente todas las clases de
 * recursos JAX-RS (clases anotadas con {@code @Path}) y los proveedores JAX-RS
 * (como filtros, interceptores, mapeadores de excepciones, etc.) que forman
 * parte de la aplicación.
 * </p>
 * <p>
 * El contenedor JAX-RS (Jersey, en este caso, configurado en {@code web.xml})
 * utilizará esta clase para descubrir los componentes de la API.
 * </p>
 *
 * @see ApplicationPath
 * @see Application
 * @see jakarta.ws.rs.Path
 * @see jakarta.ws.rs.ext.Provider
 * @author Eduardo Olalde
 */
@ApplicationPath("/api") // Define la ruta base para todos los endpoints JAX-RS de esta aplicación
public class ApplicationConfig extends Application {

    /**
     * Devuelve el conjunto de todas las clases de recursos y proveedores JAX-RS
     * que componen esta aplicación. El contenedor JAX-RS instanciará y
     * gestionará estas clases.
     *
     * @return Un {@link Set} de objetos {@link Class} representando los
     * componentes JAX-RS.
     */
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();

        // Recursos RESTful que devuelven principalmente JSON
        resources.add(FestivalResource.class);
        resources.add(UsuarioResource.class);
        resources.add(AuthResource.class);
        resources.add(PuntoVentaResource.class);
        resources.add(PublicVentaResource.class);

        // Recursos que actúan como controladores para paneles web (usan JSPs)
        resources.add(PromotorResource.class);
        resources.add(AdminResource.class);

        // Filtros, Interceptores, Mapeadores de Excepciones, etc.
        resources.add(AuthenticationFilter.class);
        resources.add(GenericExceptionMapper.class);

        return resources;
    }
}
