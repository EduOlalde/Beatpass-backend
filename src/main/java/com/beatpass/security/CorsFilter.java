package com.beatpass.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(CorsFilter.class);

    private static final Set<String> ALLOWED_ORIGINS = new HashSet<>(Arrays.asList(
            "https://eduolalde.github.io",
            "https://daaf292.github.io",
            "https://beatpass.onrender.com"
    ));

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        String requestedHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");

        log.trace("CORS Filter (Request Phase) - Processing request from Origin: {}", origin);
        log.trace("CORS Filter (Request Phase) - Requested Method: {}", requestContext.getMethod());
        log.trace("CORS Filter (Request Phase) - Requested Headers: {}", requestedHeaders);

        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
                log.debug("CORS Filter - Handling OPTIONS preflight request for Origin: {}", origin);
                requestContext.abortWith(Response.ok()
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                        .header("Access-Control-Allow-Headers", requestedHeaders != null ? requestedHeaders : "Origin, Content-Type, Accept, Authorization, X-Requested-With")
                        .header("Access-Control-Allow-Credentials", "true")
                        .header("Access-Control-Max-Age", "3600")
                        .build());
                return;
            }
        } else if (origin != null) {
            log.warn("CORS Filter - Request from unauthorized origin: {}", origin);
        } else {
            log.trace("CORS Filter - No Origin header found in request phase.");
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");

        if (origin != null && ALLOWED_ORIGINS.contains(origin) && !requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            log.trace("CORS Filter (Response Phase) - Adding Access-Control-Allow-Origin header for Origin: {}", origin);
            responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        } else if (origin != null && ALLOWED_ORIGINS.contains(origin) && requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            log.trace("CORS Filter (Response Phase) - OPTIONS request. Headers already handled in RequestFilter. Skipping.");
        }
    }
}
