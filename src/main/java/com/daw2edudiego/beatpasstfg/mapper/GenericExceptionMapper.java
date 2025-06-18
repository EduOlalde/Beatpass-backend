package com.daw2edudiego.beatpasstfg.mapper;

import com.daw2edudiego.beatpasstfg.exception.*;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapeador de excepciones global para JAX-RS. Convierte excepciones lanzadas
 * por los recursos o servicios en respuestas HTTP con formato JSON
 * estandarizado. Captura RuntimeException y excepciones WebApplicationException
 * (como NotFoundException, ForbiddenException, etc.).
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger log = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        Response.Status status;
        String message;
        Map<String, String> errorResponse = new HashMap<>();

        // Manejo específico para WebApplicationException (lanzadas directamente por JAX-RS o tus recursos)
        if (exception instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) exception;
            status = Response.Status.fromStatusCode(wae.getResponse().getStatus());
            message = wae.getMessage(); // O un mensaje más genérico si el de la excepción no es para el cliente
            log.warn("WebApplicationException capturada: Status={}, Message='{}'", status.getStatusCode(), message);
            if (message == null || message.isBlank()) {
                message = "Error de la aplicación web.";
            }
        } // Manejo de tus excepciones personalizadas
        else if (exception instanceof UsuarioNotFoundException
                || exception instanceof FestivalNotFoundException
                || exception instanceof TipoEntradaNotFoundException
                || exception instanceof EntradaNotFoundException
                || exception instanceof AsistenteNotFoundException
                || exception instanceof PulseraNFCNotFoundException) {
            status = Response.Status.NOT_FOUND; // 404
            message = exception.getMessage();
            log.warn("Excepción 'NotFound' capturada: {}", message);
        } else if (exception instanceof EmailExistenteException
                || exception instanceof StockInsuficienteException
                || exception instanceof PulseraYaAsociadaException
                || exception instanceof EntradaNoNominadaException) {
            status = Response.Status.CONFLICT; // 409
            message = exception.getMessage();
            log.warn("Excepción de 'Conflicto de Negocio' capturada: {}", message);
        } else if (exception instanceof IllegalArgumentException
                || exception instanceof IllegalStateException
                || exception instanceof PagoInvalidoException
                || exception instanceof FestivalNoPublicadoException
                || exception instanceof PasswordIncorrectoException) {
            status = Response.Status.BAD_REQUEST; // 400
            message = exception.getMessage();
            log.warn("Excepción de 'Bad Request' o 'Estado Ilegal' capturada: {}", message);
        } else if (exception instanceof SecurityException || exception instanceof ForbiddenException) {
            status = Response.Status.FORBIDDEN; // 403
            message = exception.getMessage();
            log.warn("Excepción de 'Acceso Denegado' capturada: {}", message);
        } else if (exception instanceof NotAuthorizedException) {
            status = Response.Status.UNAUTHORIZED; // 401
            message = exception.getMessage();
            log.warn("Excepción de 'No Autorizado' capturada: {}", message);
        } // Excepción genérica para errores no previstos
        else {
            status = Response.Status.INTERNAL_SERVER_ERROR; // 500
            message = "Error interno inesperado del servidor."; // Mensaje genérico para el cliente
            log.error("Error inesperado en la aplicación: {}", exception.getMessage(), exception); // Log completo para el servidor
        }

        errorResponse.put("error", message);
        return Response.status(status)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
