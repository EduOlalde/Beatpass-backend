package com.beatpass.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provee una instancia personalizada de ObjectMapper para Jackson, configurada
 * para serializar y deserializar correctamente tipos de Java Time (JSR-310).
 *
 * @author Eduardo Olalde
 */
@Provider // Indica a JAX-RS que esta clase es un proveedor (ej. un mapeador de contexto)
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private static final Logger log = LoggerFactory.getLogger(ObjectMapperContextResolver.class);
    private final ObjectMapper mapper;

    public ObjectMapperContextResolver() {
        log.info("Inicializando ObjectMapperContextResolver para Jackson...");
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Desactivar la escritura de fechas como timestamps UNIX (números)
        // y forzar la escritura como strings ISO 8601.
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        log.info("ObjectMapper configurado: JavaTimeModule registrado y WRITE_DATES_AS_TIMESTAMPS=false.");
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
