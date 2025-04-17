/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.FestivalService;
import com.daw2edudiego.beatpasstfg.service.FestivalServiceImpl;

import jakarta.validation.Valid; // Para validar DTOs si usas Bean Validation
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Recurso JAX-RS para gestionar Festivales.
 */
@Path("/festivales")
@Produces(MediaType.APPLICATION_JSON) // Por defecto, todos los métodos devolverán JSON
@Consumes(MediaType.APPLICATION_JSON) // Por defecto, esperan recibir JSON (donde aplique)
public class FestivalResource {

    private static final Logger log = LoggerFactory.getLogger(FestivalResource.class);

    private final FestivalService festivalService;

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    public FestivalResource() {
        this.festivalService = new FestivalServiceImpl();
    }

    @POST
    public Response crearFestival(@Valid FestivalDTO festivalDTO) { // @Valid activaría Bean Validation si está configurado
        log.info("POST /festivales recibido");
        Integer idPromotorAutenticado = obtenerIdUsuarioAutenticado(); // Asume rol PROMOTOR internamente o valida
        if (idPromotorAutenticado == null || !esRol(RolUsuario.PROMOTOR)) {
             log.warn("Acceso no autorizado a POST /festivales");
             return Response.status(Response.Status.UNAUTHORIZED).entity("Acceso denegado.").build();
        }

        try {
            // Validaciones básicas adicionales si @Valid no es suficiente
             if (festivalDTO == null || festivalDTO.getNombre() == null || festivalDTO.getNombre().isBlank()) {
                 log.warn("Intento de crear festival con nombre vacío.");
                 return Response.status(Response.Status.BAD_REQUEST).entity("El nombre del festival es obligatorio.").build();
            }
             // ... otras validaciones ...

            FestivalDTO festivalCreado = festivalService.crearFestival(festivalDTO, idPromotorAutenticado);
            URI location = uriInfo.getAbsolutePathBuilder().path(festivalCreado.getIdFestival().toString()).build();
            log.info("Festival creado con ID: {}, Location: {}", festivalCreado.getIdFestival(), location);
            return Response.created(location).entity(festivalCreado).build();

        } catch (IllegalArgumentException | SecurityException e) {
             log.warn("Error de validación/permiso al crear festival: {}", e.getMessage());
             return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Error interno al crear festival: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al crear el festival.").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response obtenerFestival(@PathParam("id") Integer id) {
        log.info("GET /festivales/{} recibido", id);
        Optional<FestivalDTO> festivalOpt = festivalService.obtenerFestivalPorId(id);

        return festivalOpt
                .map(dto -> {
                    log.debug("Festival ID {} encontrado.", id);
                    return Response.ok(dto).build();
                })
                .orElseGet(() -> {
                     log.warn("Festival ID {} no encontrado.", id);
                     return Response.status(Response.Status.NOT_FOUND).entity("Festival no encontrado.").build();
                });
    }

    @PUT
    @Path("/{id}")
    public Response actualizarFestival(@PathParam("id") Integer id, @Valid FestivalDTO festivalDTO) {
        log.info("PUT /festivales/{} recibido", id);
        Integer idPromotorAutenticado = obtenerIdUsuarioAutenticado();
         if (idPromotorAutenticado == null || !esRol(RolUsuario.PROMOTOR)) {
             log.warn("Acceso no autorizado a PUT /festivales/{}", id);
             return Response.status(Response.Status.UNAUTHORIZED).entity("Acceso denegado.").build();
        }

        try {
             if (festivalDTO == null || festivalDTO.getNombre() == null || festivalDTO.getNombre().isBlank()) {
                 log.warn("Intento de actualizar festival ID {} con nombre vacío.", id);
                 return Response.status(Response.Status.BAD_REQUEST).entity("El nombre del festival es obligatorio.").build();
            }
             // ... otras validaciones ...

            FestivalDTO festivalActualizado = festivalService.actualizarFestival(id, festivalDTO, idPromotorAutenticado);
            log.info("Festival ID {} actualizado.", id);
            return Response.ok(festivalActualizado).build();

        } catch (FestivalNotFoundException e) {
            log.warn("Festival ID {} no encontrado para actualizar.", id);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (SecurityException e) {
            log.warn("Intento no autorizado de actualizar festival ID {} por usuario {}", id, idPromotorAutenticado);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
             log.warn("Error de validación al actualizar festival ID {}: {}", id, e.getMessage());
             return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Error interno al actualizar festival ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al actualizar el festival.").build();
        }
    }

     @DELETE
    @Path("/{id}")
    public Response eliminarFestival(@PathParam("id") Integer id) {
        log.info("DELETE /festivales/{} recibido", id);
         Integer idPromotorAutenticado = obtenerIdUsuarioAutenticado();
         if (idPromotorAutenticado == null || !esRol(RolUsuario.PROMOTOR)) {
             log.warn("Acceso no autorizado a DELETE /festivales/{}", id);
             return Response.status(Response.Status.UNAUTHORIZED).entity("Acceso denegado.").build();
        }

        try {
            festivalService.eliminarFestival(id, idPromotorAutenticado);
            log.info("Festival ID {} eliminado.", id);
            return Response.noContent().build(); // 204

        } catch (FestivalNotFoundException e) {
             log.warn("Festival ID {} no encontrado para eliminar.", id);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (SecurityException e) {
             log.warn("Intento no autorizado de eliminar festival ID {} por usuario {}", id, idPromotorAutenticado);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (RuntimeException e) { // Captura genérica para errores de FK, etc.
            log.error("Error al eliminar festival ID {}: {}", id, e.getMessage(), e);
            // Podría ser un 409 Conflict si no se puede borrar por dependencias
            return Response.status(Response.Status.CONFLICT).entity("No se pudo eliminar el festival: " + e.getMessage()).build();
        } catch (Exception e) {
            log.error("Error interno al eliminar festival ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al eliminar el festival.").build();
        }
    }

    @GET
    @Path("/publicados")
    public Response buscarFestivalesPublicados(
            @QueryParam("fechaDesde") String fechaDesdeStr,
            @QueryParam("fechaHasta") String fechaHastaStr) {
        log.info("GET /festivales/publicados recibido con fechas: {} - {}", fechaDesdeStr, fechaHastaStr);
        LocalDate fechaDesde;
        LocalDate fechaHasta;
        try {
            fechaDesde = (fechaDesdeStr != null && !fechaDesdeStr.isBlank()) ? LocalDate.parse(fechaDesdeStr) : LocalDate.now();
            fechaHasta = (fechaHastaStr != null && !fechaHastaStr.isBlank()) ? LocalDate.parse(fechaHastaStr) : fechaDesde.plusYears(1);
            if(fechaHasta.isBefore(fechaDesde)){
                 log.warn("Fechas inválidas en GET /festivales/publicados: fechaHasta < fechaDesde");
                 return Response.status(Response.Status.BAD_REQUEST).entity("La fecha 'hasta' no puede ser anterior a la fecha 'desde'.").build();
            }
        } catch (DateTimeParseException e) {
             log.warn("Formato de fecha inválido en GET /festivales/publicados: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Formato de fecha inválido. Use yyyy-MM-dd.").build();
        }

        List<FestivalDTO> festivales = festivalService.buscarFestivalesPublicados(fechaDesde, fechaHasta);
        log.info("Devolviendo {} festivales publicados.", festivales.size());
        return Response.ok(festivales).build();
    }


    @GET
    @Path("/mis-festivales")
    public Response obtenerMisFestivales() {
        log.info("GET /festivales/mis-festivales recibido");
        Integer idPromotorAutenticado = obtenerIdUsuarioAutenticado();
         if (idPromotorAutenticado == null || !esRol(RolUsuario.PROMOTOR)) {
             log.warn("Acceso no autorizado a GET /festivales/mis-festivales");
             return Response.status(Response.Status.UNAUTHORIZED).entity("Acceso denegado.").build();
        }

        List<FestivalDTO> festivales = festivalService.obtenerFestivalesPorPromotor(idPromotorAutenticado);
        log.info("Devolviendo {} festivales para el promotor ID {}", festivales.size(), idPromotorAutenticado);
        return Response.ok(festivales).build();
    }

    @PUT
    @Path("/{id}/estado")
    public Response cambiarEstadoFestival(
                @PathParam("id") Integer id,
                @QueryParam("nuevoEstado") String nuevoEstadoStr) {
        log.info("PUT /festivales/{}/estado recibido con nuevoEstado={}", id, nuevoEstadoStr);
         Integer idPromotorAutenticado = obtenerIdUsuarioAutenticado();
         if (idPromotorAutenticado == null || !esRol(RolUsuario.PROMOTOR)) {
             log.warn("Acceso no autorizado a PUT /festivales/{}/estado", id);
             return Response.status(Response.Status.UNAUTHORIZED).entity("Acceso denegado.").build();
        }

        EstadoFestival nuevoEstado;
        try {
             if (nuevoEstadoStr == null || nuevoEstadoStr.isBlank()) {
                 throw new IllegalArgumentException("El parámetro 'nuevoEstado' es obligatorio.");
             }
            nuevoEstado = EstadoFestival.valueOf(nuevoEstadoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Valor inválido para 'nuevoEstado' en PUT /festivales/{}/estado: {}", id, nuevoEstadoStr);
            return Response.status(Response.Status.BAD_REQUEST).entity("Valor de 'nuevoEstado' inválido. Valores posibles: BORRADOR, PUBLICADO, CANCELADO, FINALIZADO.").build();
        }

        try {
            FestivalDTO festivalActualizado = festivalService.cambiarEstadoFestival(id, nuevoEstado, idPromotorAutenticado);
            log.info("Estado de festival ID {} cambiado a {}.", id, nuevoEstado);
            return Response.ok(festivalActualizado).build();

        } catch (FestivalNotFoundException e) {
            log.warn("Festival ID {} no encontrado para cambiar estado.", id);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (SecurityException e) {
             log.warn("Intento no autorizado de cambiar estado a festival ID {} por usuario {}", id, idPromotorAutenticado);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Error interno al cambiar estado de festival ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al cambiar estado del festival.").build();
        }
    }


    // --- Métodos Auxiliares (Simulados/Placeholder) ---

    /**
     * Simula la obtención del ID del usuario autenticado desde el SecurityContext.
     * ¡Reemplazar con lógica real de seguridad!
     */
    private Integer obtenerIdUsuarioAutenticado() {
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            try {
                // Asume que el nombre principal es el ID del usuario como String
                return Integer.parseInt(securityContext.getUserPrincipal().getName());
            } catch (NumberFormatException e) {
                log.error("Error al parsear ID de usuario desde SecurityContext Principal: {}", securityContext.getUserPrincipal().getName());
                return null;
            }
        }
        log.warn("SecurityContext o UserPrincipal es null. Usando ID de usuario fijo (1) para pruebas.");
        return 1; // ¡¡¡ SOLO PARA DESARROLLO/PRUEBAS SIN SEGURIDAD !!!
        // return null; // En un entorno real sin autenticación, devolver null.
    }

     /**
     * Simula la comprobación de rol del usuario autenticado.
      * ¡Reemplazar con lógica real de seguridad!
     */
    private boolean esRol(RolUsuario rol) {
         if (securityContext != null) {
             // Asume que tienes un mecanismo para verificar roles
             // return securityContext.isUserInRole(rol.name());
             log.warn("Comprobación de rol simulada. Asumiendo que el usuario tiene el rol {} para pruebas.", rol);
             return true; // ¡¡¡ SOLO PARA DESARROLLO/PRUEBAS SIN SEGURIDAD !!!
         }
          log.warn("SecurityContext es null. No se puede verificar el rol.");
         return false;
    }

}
