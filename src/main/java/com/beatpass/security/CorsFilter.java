package com.beatpass.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filtro JAX-RS para añadir los encabezados CORS a las respuestas. Permite
 * orígenes específicos y maneja las peticiones OPTIONS (preflight).
 */
@Provider
@PreMatching
public class CorsFilter implements ContainerResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(CorsFilter.class);

    private static final Set<String> ALLOWED_ORIGINS = new HashSet<>(Arrays.asList(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "https://eduolalde.github.io",
            "https://daaf292.github.io",
            "https://daw2-tfg-beatpass.onrender.com"
    ));

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        String requestedHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");

        log.trace("CORS Filter - Processing request from Origin: {}", origin);
        log.trace("CORS Filter - Requested Method: {}", requestContext.getMethod());
        log.trace("CORS Filter - Requested Headers: {}", requestedHeaders);

        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");

            if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
                log.debug("CORS Filter - Handling OPTIONS preflight request for Origin: {}", origin);
                responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

                if (requestedHeaders != null && !requestedHeaders.isEmpty()) {
                    responseContext.getHeaders().add("Access-Control-Allow-Headers", requestedHeaders);
                } else {
                    responseContext.getHeaders().add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");
                }
                responseContext.getHeaders().add("Access-Control-Max-Age", "3600");

                if (responseContext.getStatus() == 0 || responseContext.getStatus() == 204) {
                    responseContext.setStatus(200);
                }
            }
        } else if (origin != null) {
            log.warn("CORS Filter - Request from unauthorized origin: {}", origin);
        } else {
            log.trace("CORS Filter - No Origin header found.");
        }
    }
}
