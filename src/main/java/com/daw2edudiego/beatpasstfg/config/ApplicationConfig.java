package com.daw2edudiego.beatpasstfg.config;

import com.daw2edudiego.beatpasstfg.security.AuthenticationFilter;
import com.daw2edudiego.beatpasstfg.web.*; // Importar todos los recursos web

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

        // --- Registrar Clases de Recursos (@Path) ---
        // Recursos RESTful que devuelven principalmente JSON
        resources.add(FestivalResource.class);      // Endpoints para /api/festivales
        resources.add(UsuarioResource.class);       // Endpoints para /api/usuarios
        resources.add(AuthResource.class);          // Endpoints para /api/auth (login JWT)
        resources.add(PuntoVentaResource.class);    // Endpoints para /api/pos (operaciones POS/NFC)
        resources.add(PublicVentaResource.class);   // Endpoints para /api/public/venta ---


        // Recursos que actúan como controladores para paneles web (usan JSPs)
        // Aunque usen Servlets/JSP internamente, se registran si tienen anotaciones JAX-RS (@Path, @GET, etc.)
        resources.add(PromotorResource.class);      // Endpoints para /api/promotor (panel promotor)
        resources.add(AdminResource.class);         // Endpoints para /api/admin (panel admin)

        // --- Registrar Clases Proveedoras (@Provider) ---
        // Filtros, Interceptores, Mapeadores de Excepciones, etc.
        resources.add(AuthenticationFilter.class);  // Filtro para validar tokens JWT

        // Añadir aquí otros providers si se crean, por ejemplo:
        // resources.add(JsonProcessingExceptionMapper.class);
        // resources.add(ConstraintViolationExceptionMapper.class);
        // resources.add(LoggingFilter.class);
        return resources;
    }
}
