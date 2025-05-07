package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.*;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Recurso JAX-RS para el panel web del Promotor (/api/promotor). Gestiona
 * Festivales, Entradas, Asistentes y Pulseras propias. Requiere rol PROMOTOR en
 * sesión HTTP. Devuelve principalmente HTML (JSPs).
 */
@Path("/promotor")
public class PromotorResource {

    private static final Logger log = LoggerFactory.getLogger(PromotorResource.class);

    private final FestivalService festivalService;
    private final UsuarioService usuarioService;
    private final EntradaService entradaService;
    private final EntradaAsignadaService entradaAsignadaService;
    // private final VentaService ventaService; // VentaService no se usa directamente aquí
    private final AsistenteService asistenteService;
    private final PulseraNFCService pulseraNFCService;
    private final CompraService compraService;

    @Context
    private UriInfo uriInfo;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    public PromotorResource() {
        this.festivalService = new FestivalServiceImpl();
        this.usuarioService = new UsuarioServiceImpl();
        this.entradaService = new EntradaServiceImpl();
        this.entradaAsignadaService = new EntradaAsignadaServiceImpl();
        // this.ventaService = new VentaServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.pulseraNFCService = new PulseraNFCServiceImpl();
        this.compraService = new CompraServiceImpl();
    }

    // --- Gestión de Festivales ---
    @GET
    @Path("/festivales")
    @Produces(MediaType.TEXT_HTML)
    public Response listarFestivales() throws ServletException, IOException {
        log.debug("GET /promotor/festivales");
        Integer idPromotor = verificarAccesoPromotor(request);
        List<FestivalDTO> listaFestivales = festivalService.obtenerFestivalesPorPromotor(idPromotor);

        request.setAttribute("festivales", listaFestivales);
        request.setAttribute("idPromotorAutenticado", idPromotor);
        mostrarMensajeFlash(request);
        forwardToJsp("/WEB-INF/jsp/promotor/mis-festivales.jsp");
        return Response.ok().build();
    }

    @GET
    @Path("/festivales/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrear() throws ServletException, IOException {
        log.debug("GET /promotor/festivales/crear");
        Integer idPromotor = verificarAccesoPromotor(request);
        request.setAttribute("festival", new FestivalDTO());
        request.setAttribute("idPromotorAutenticado", idPromotor);
        request.setAttribute("esNuevo", true);
        forwardToJsp("/WEB-INF/jsp/promotor/promotor-festival-editar.jsp");
        return Response.ok().build();
    }

    @GET
    @Path("/festivales/ver/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarDetallesFestival(@PathParam("id") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/ver/{}", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null) {
            throw new BadRequestException("ID festival no válido.");
        }

        try {
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            List<EntradaDTO> listaEntradas = entradaService.obtenerEntradasPorFestival(idFestival, idPromotor);

            request.setAttribute("festival", festival);
            request.setAttribute("tiposEntrada", listaEntradas);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);
            forwardToJsp("/WEB-INF/jsp/promotor/festival-detalle.jsp");
            return Response.ok().build();

        } catch (NotFoundException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al mostrar detalles festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del festival", e);
        }
    }

    @GET
    @Path("/festivales/editar/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioEditarFestival(@PathParam("id") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/editar/{}", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null) {
            throw new BadRequestException("ID festival no válido.");
        }

        try {
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            request.setAttribute("festival", festival);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            request.setAttribute("esNuevo", false);
            mostrarMensajeFlash(request);
            forwardToJsp("/WEB-INF/jsp/promotor/promotor-festival-editar.jsp");
            return Response.ok().build();

        } catch (NotFoundException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al mostrar form editar festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del festival para editar", e);
        }
    }

    @POST
    @Path("/festivales/guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarFestival(
            @FormParam("idFestival") String idStr, @FormParam("nombre") String nombre,
            @FormParam("descripcion") String descripcion, @FormParam("fechaInicio") String fechaInicioStr,
            @FormParam("fechaFin") String fechaFinStr, @FormParam("ubicacion") String ubicacion,
            @FormParam("aforo") String aforoStr, @FormParam("imagenUrl") String imagenUrl
    ) throws ServletException, IOException {

        log.info("POST /promotor/festivales/guardar");
        Integer idPromotor = verificarAccesoPromotor(request);
        boolean esNuevo = (idStr == null || idStr.isEmpty() || "0".equals(idStr));
        Integer idFestival = null;
        FestivalDTO dto = new FestivalDTO();
        String errorMessage = null;
        FestivalDTO festivalGuardado = null;

        try {
            if (!esNuevo) {
                idFestival = Integer.parseInt(idStr);
            }
            dto.setIdFestival(idFestival);

            // Validar y poblar DTO (Simplificado, asume parseo funciona o lanza excepción)
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("Nombre obligatorio.");
            }
            dto.setNombre(nombre);
            dto.setDescripcion(descripcion);
            dto.setUbicacion(ubicacion);
            dto.setImagenUrl(imagenUrl);
            if (fechaInicioStr == null || fechaFinStr == null || fechaInicioStr.isBlank() || fechaFinStr.isBlank()) {
                throw new IllegalArgumentException("Fechas obligatorias.");
            }
            dto.setFechaInicio(LocalDate.parse(fechaInicioStr));
            dto.setFechaFin(LocalDate.parse(fechaFinStr));
            if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
                throw new IllegalArgumentException("Fecha fin no puede ser anterior a inicio.");
            }
            if (aforoStr != null && !aforoStr.isBlank()) {
                dto.setAforo(Integer.parseInt(aforoStr));
                if (dto.getAforo() <= 0) {
                    throw new IllegalArgumentException("Aforo debe ser positivo.");
                }
            }

            String mensajeExito;
            if (esNuevo) {
                festivalGuardado = festivalService.crearFestival(dto, idPromotor);
                idFestival = festivalGuardado.getIdFestival();
                mensajeExito = "Solicitud de festival '" + festivalGuardado.getNombre() + "' creada (BORRADOR).";
            } else {
                festivalService.actualizarFestival(idFestival, dto, idPromotor);
                festivalGuardado = dto; // Usar DTO para mensaje
                mensajeExito = "Festival '" + dto.getNombre() + "' actualizado.";
            }

            setFlashMessage(request, "mensaje", mensajeExito);
            URI viewUri = buildUri("mostrarDetallesFestival", idFestival);
            return Response.seeOther(viewUri).build();

        } catch (NumberFormatException e) {
            errorMessage = "Formato numérico inválido (ID o Aforo).";
            log.warn("Error guardando festival (promotor {}): {}", idPromotor, errorMessage);
        } catch (DateTimeParseException e) {
            errorMessage = "Formato de fecha inválido (use yyyy-MM-dd).";
            log.warn("Error guardando festival (promotor {}): {}", idPromotor, errorMessage);
        } catch (IllegalArgumentException | FestivalNotFoundException | SecurityException | IllegalStateException e) {
            errorMessage = e.getMessage();
            log.warn("Error guardando festival (promotor {}): {}", idPromotor, errorMessage);
        } catch (Exception e) {
            errorMessage = "Error interno inesperado al guardar el festival.";
            log.error("Error interno guardando festival (promotor {}): {}", idPromotor, e.getMessage(), e);
        }

        // Si hubo error, volver al form
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        dto.setUbicacion(ubicacion);
        dto.setImagenUrl(imagenUrl);
        dto.setIdPromotor(idPromotor);
        try {
            dto.setFechaInicio(LocalDate.parse(fechaInicioStr));
        } catch (Exception ignored) {
        }
        try {
            dto.setFechaFin(LocalDate.parse(fechaFinStr));
        } catch (Exception ignored) {
        }
        try {
            if (aforoStr != null && !aforoStr.isBlank()) {
                dto.setAforo(Integer.parseInt(aforoStr));
            }
        } catch (Exception ignored) {
        }

        forwardToPromotorFestivalEditFormWithError(dto, idPromotor, !esNuevo, errorMessage);
        return Response.ok().build();
    }

    // --- Gestión de Tipos de Entrada ---
    @POST
    @Path("/festivales/{idFestival}/entradas")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarEntrada(
            @PathParam("idFestival") Integer idFestival,
            @FormParam("tipo") String tipo, @FormParam("descripcion") String descripcion,
            @FormParam("precio") String precioStr, @FormParam("stock") String stockStr) {

        log.info("POST /promotor/festivales/{}/entradas", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null) {
            throw new BadRequestException("ID festival inválido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        EntradaDTO dto = new EntradaDTO(); // Para posible reenvío

        try {
            if (tipo == null || tipo.isBlank()) {
                throw new IllegalArgumentException("Tipo de entrada obligatorio.");
            }
            if (precioStr == null || precioStr.isBlank()) {
                throw new IllegalArgumentException("Precio obligatorio.");
            }
            if (stockStr == null || stockStr.isBlank()) {
                throw new IllegalArgumentException("Stock obligatorio.");
            }

            dto.setIdFestival(idFestival);
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
            dto.setPrecio(new BigDecimal(precioStr.replace(',', '.')));
            dto.setStock(Integer.parseInt(stockStr));
            if (dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Precio no negativo.");
            }
            if (dto.getStock() < 0) {
                throw new IllegalArgumentException("Stock no negativo.");
            }

            EntradaDTO entradaCreada = entradaService.crearEntrada(dto, idFestival, idPromotor);
            mensajeFlash = "Tipo de entrada '" + entradaCreada.getTipo() + "' añadido.";

        } catch (NumberFormatException e) {
            errorFlash = "Formato numérico inválido para precio o stock.";
            log.warn("Error de formato numérico al crear entrada para festival {}: {}", idFestival, e.getMessage());
            // Poblar DTO con datos erróneos para repintar form
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
        } catch (IllegalArgumentException | SecurityException | FestivalNotFoundException e) {
            errorFlash = "Error al añadir entrada: " + e.getMessage();
            log.warn("Error de validación/negocio al crear entrada para festival {}: {}", idFestival, errorFlash);
            // Poblar DTO con datos erróneos para repintar form
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
            try {
                dto.setPrecio(new BigDecimal(precioStr.replace(',', '.')));
            } catch (Exception ignored) {
            }
            try {
                dto.setStock(Integer.parseInt(stockStr));
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al añadir el tipo de entrada.";
            log.error("Error interno al crear entrada para festival {}: {}", idFestival, e.getMessage(), e);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            if (mensajeFlash != null) {
                session.setAttribute("mensaje", mensajeFlash);
            }
            if (errorFlash != null) {
                session.setAttribute("error", errorFlash);
                session.setAttribute("nuevaEntradaConError", dto);
                session.setAttribute("errorEntrada", true);
            }
        }

        URI detailUri = buildUri("mostrarDetallesFestival", idFestival);
        return Response.seeOther(detailUri).build();
    }

    @GET
    @Path("/entradas/{idEntrada}/editar")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioEditarEntrada(@PathParam("idEntrada") Integer idEntrada) throws ServletException, IOException {
        log.debug("GET /promotor/entradas/{}/editar", idEntrada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idEntrada == null) {
            throw new BadRequestException("ID entrada no válido.");
        }

        try {
            EntradaDTO entradaDTO = entradaService.obtenerEntradaPorId(idEntrada, idPromotor)
                    .orElseThrow(() -> new NotFoundException("Tipo de entrada no encontrado o sin permiso."));

            request.setAttribute("entrada", entradaDTO);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);
            forwardToJsp("/WEB-INF/jsp/promotor/promotor-entrada-detalle.jsp");
            return Response.ok().build();

        } catch (NotFoundException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al mostrar form editar entrada ID {}: {}", idEntrada, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del tipo de entrada.", e);
        }
    }

    @POST
    @Path("/entradas/{idEntrada}/actualizar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response actualizarEntrada(
            @PathParam("idEntrada") Integer idEntrada,
            @FormParam("tipo") String tipo, @FormParam("descripcion") String descripcion,
            @FormParam("precio") String precioStr, @FormParam("stock") String stockStr) throws ServletException, IOException {

        log.info("POST /promotor/entradas/{}/actualizar", idEntrada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idEntrada == null) {
            throw new BadRequestException("ID entrada no válido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null;
        EntradaDTO dto = new EntradaDTO();

        try {
            if (tipo == null || tipo.isBlank()) {
                throw new IllegalArgumentException("Tipo obligatorio.");
            }
            if (precioStr == null || precioStr.isBlank()) {
                throw new IllegalArgumentException("Precio obligatorio.");
            }
            if (stockStr == null || stockStr.isBlank()) {
                throw new IllegalArgumentException("Stock obligatorio.");
            }

            dto.setIdEntrada(idEntrada);
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
            dto.setPrecio(new BigDecimal(precioStr.replace(',', '.')));
            dto.setStock(Integer.parseInt(stockStr));
            if (dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Precio no negativo.");
            }
            if (dto.getStock() < 0) {
                throw new IllegalArgumentException("Stock no negativo.");
            }

            EntradaDTO actualizada = entradaService.actualizarEntrada(idEntrada, dto, idPromotor);
            mensajeFlash = "Tipo de entrada '" + actualizada.getTipo() + "' actualizado.";
            idFestival = actualizada.getIdFestival();

        } catch (NumberFormatException e) {
            errorFlash = "Formato numérico inválido para precio o stock.";
            log.warn("Error de formato numérico al actualizar entrada ID {}: {}", idEntrada, e.getMessage());
            // Poblar DTO para reenvío
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
        } catch (IllegalArgumentException | SecurityException | EntradaNotFoundException e) {
            errorFlash = "Error al actualizar: " + e.getMessage();
            log.warn("Error de validación/negocio al actualizar entrada ID {}: {}", idEntrada, errorFlash);
            // Poblar DTO para reenvío
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
            try {
                dto.setPrecio(new BigDecimal(precioStr.replace(',', '.')));
            } catch (Exception ignored) {
            }
            try {
                dto.setStock(Integer.parseInt(stockStr));
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al actualizar el tipo de entrada.";
            log.error("Error interno al actualizar entrada ID {}: {}", idEntrada, e.getMessage(), e);
        }

        setFlashMessage(request, errorFlash != null ? "error" : "mensaje", errorFlash != null ? errorFlash : mensajeFlash);

        URI redirectUri;
        if (errorFlash != null) {
            redirectUri = buildUri("mostrarFormularioEditarEntrada", idEntrada); // Volver a edición
        } else {
            redirectUri = buildUri("mostrarDetallesFestival", idFestival); // Ir a vista festival
        }
        return Response.seeOther(redirectUri).build();
    }

    @POST
    @Path("/entradas/{idEntrada}/eliminar")
    public Response eliminarEntrada(@PathParam("idEntrada") Integer idEntrada) {
        log.info("POST /promotor/entradas/{}/eliminar", idEntrada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idEntrada == null) {
            throw new BadRequestException("ID entrada no válido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null;

        try {
            Optional<EntradaDTO> optDto = entradaService.obtenerEntradaPorId(idEntrada, idPromotor);
            if (optDto.isPresent()) {
                idFestival = optDto.get().getIdFestival();
                entradaService.eliminarEntrada(idEntrada, idPromotor);
                mensajeFlash = "Tipo de entrada ID " + idEntrada + " eliminado.";
            } else {
                errorFlash = "Entrada no encontrada o sin permiso.";
            }
        } catch (EntradaNotFoundException | UsuarioNotFoundException e) {
            errorFlash = e.getMessage();
            log.warn("Error al eliminar entrada ID {}: {}", idEntrada, errorFlash);
        } catch (SecurityException e) {
            errorFlash = e.getMessage();
            log.warn("Error de seguridad al eliminar entrada ID {}: {}", idEntrada, errorFlash);
        } catch (RuntimeException e) {
            errorFlash = "No se pudo eliminar el tipo de entrada (posiblemente tiene ventas asociadas): " + e.getMessage();
            log.error("Error runtime al eliminar entrada ID {}: {}", idEntrada, e.getMessage(), e);
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al eliminar.";
            log.error("Error interno al eliminar entrada ID {}: {}", idEntrada, e.getMessage(), e);
        }

        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
        URI redirectUri = (idFestival != null) ? buildUri("mostrarDetallesFestival", idFestival) : buildUri("listarFestivales", null);
        return Response.seeOther(redirectUri).build();
    }

    // --- Gestión de Entradas Asignadas ---
    @GET
    @Path("/festivales/{idFestival}/entradas-asignadas")
    @Produces(MediaType.TEXT_HTML)
    public Response listarEntradasAsignadas(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/{}/entradas-asignadas", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null) {
            throw new BadRequestException("ID festival inválido.");
        }
        try {
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            List<EntradaAsignadaDTO> listaEntradas = entradaAsignadaService.obtenerEntradasAsignadasPorFestival(idFestival, idPromotor);

            request.setAttribute("festival", festival);
            request.setAttribute("entradasAsignadas", listaEntradas);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);
            forwardToJsp("/WEB-INF/jsp/promotor/promotor-entradas-asignadas.jsp");
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al listar entradas asignadas festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar entradas asignadas.", e);
        }
    }

    @POST
    @Path("/entradas-asignadas/{idEntradaAsignada}/nominar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response nominarEntrada(
            @PathParam("idEntradaAsignada") Integer idEntradaAsignada,
            @FormParam("emailAsistente") String emailAsistente,
            @FormParam("nombreAsistente") String nombreAsistente,
            @FormParam("telefonoAsistente") String telefonoAsistente) {

        log.info("POST /promotor/entradas-asignadas/{}/nominar a email {}", idEntradaAsignada, emailAsistente);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idEntradaAsignada == null || emailAsistente == null || emailAsistente.isBlank()) {
            throw new BadRequestException("Faltan parámetros (idEntradaAsignada, emailAsistente).");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null;

        try {
            entradaAsignadaService.nominarEntrada(idEntradaAsignada, emailAsistente, nombreAsistente, telefonoAsistente, idPromotor);
            mensajeFlash = "Entrada ID " + idEntradaAsignada + " nominada a " + emailAsistente + ".";
            idFestival = entradaAsignadaService.obtenerEntradaAsignadaPorId(idEntradaAsignada, idPromotor)
                    .map(EntradaAsignadaDTO::getIdFestival).orElse(null);
        } catch (Exception e) { // Captura todas las de negocio/runtime
            errorFlash = "No se pudo nominar: " + e.getMessage();
            log.warn("Error al nominar entrada ID {}: {}", idEntradaAsignada, errorFlash);
            // Intentar obtener ID de festival para redirigir igual
            try {
                idFestival = entradaAsignadaService.obtenerEntradaAsignadaPorId(idEntradaAsignada, idPromotor)
                        .map(EntradaAsignadaDTO::getIdFestival).orElse(null);
            } catch (Exception ignored) {
            }
        }

        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
        URI redirectUri = (idFestival != null) ? buildUri("listarEntradasAsignadas", idFestival) : buildUri("listarFestivales", null);
        return Response.seeOther(redirectUri).build();
    }

    @POST
    @Path("/entradas-asignadas/{idEntradaAsignada}/cancelar")
    public Response cancelarEntrada(@PathParam("idEntradaAsignada") Integer idEntradaAsignada) {
        log.info("POST /promotor/entradas-asignadas/{}/cancelar", idEntradaAsignada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idEntradaAsignada == null) {
            throw new BadRequestException("Falta idEntradaAsignada.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null;

        try {
            idFestival = entradaAsignadaService.obtenerEntradaAsignadaPorId(idEntradaAsignada, idPromotor)
                    .map(EntradaAsignadaDTO::getIdFestival).orElse(null);
            if (idFestival != null) { // Solo intentar cancelar si se encontró y tenemos permiso
                entradaAsignadaService.cancelarEntrada(idEntradaAsignada, idPromotor);
                mensajeFlash = "Entrada ID " + idEntradaAsignada + " cancelada (stock restaurado).";
            } else {
                errorFlash = "Entrada no encontrada o sin permiso.";
            }
        } catch (Exception e) {
            errorFlash = "No se pudo cancelar: " + e.getMessage();
            log.warn("Error al cancelar entrada ID {}: {}", idEntradaAsignada, e.getMessage());
            // Si ya teníamos el ID de festival, lo mantenemos para redirigir
        }

        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
        URI redirectUri = (idFestival != null) ? buildUri("listarEntradasAsignadas", idFestival) : buildUri("listarFestivales", null);
        return Response.seeOther(redirectUri).build();
    }

    @POST
    @Path("/entradas-asignadas/{idEntradaAsignada}/asociar-pulsera")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response asociarPulseraPromotor(
            @PathParam("idEntradaAsignada") Integer idEntradaAsignada,
            @FormParam("codigoUid") String codigoUid) {

        log.info("POST /promotor/entradas-asignadas/{}/asociar-pulsera con UID: {}", idEntradaAsignada, codigoUid);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idEntradaAsignada == null || codigoUid == null || codigoUid.isBlank()) {
            throw new BadRequestException("Faltan parámetros (idEntradaAsignada, codigoUid).");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null;

        try {
            idFestival = entradaAsignadaService.obtenerEntradaAsignadaPorId(idEntradaAsignada, idPromotor)
                    .map(EntradaAsignadaDTO::getIdFestival).orElse(null);
            if (idFestival != null) {
                pulseraNFCService.asociarPulseraEntrada(codigoUid, idEntradaAsignada, idPromotor);
                mensajeFlash = "Pulsera UID '" + codigoUid + "' asociada a entrada ID " + idEntradaAsignada + ".";
            } else {
                errorFlash = "Entrada no encontrada o sin permiso.";
            }
        } catch (Exception e) {
            errorFlash = "No se pudo asociar: " + e.getMessage();
            log.warn("Error al asociar pulsera a entrada ID {}: {}", idEntradaAsignada, errorFlash);
        }

        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
        URI redirectUri = (idFestival != null) ? buildUri("listarEntradasAsignadas", idFestival) : buildUri("listarFestivales", null);
        return Response.seeOther(redirectUri).build();
    }

    // --- Listados Relacionados ---
    @GET
    @Path("/festivales/{idFestival}/asistentes")
    @Produces(MediaType.TEXT_HTML)
    public Response listarAsistentesPorFestival(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/{}/asistentes", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null) {
            throw new BadRequestException("ID festival no válido.");
        }

        try {
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));
            List<AsistenteDTO> listaAsistentes = asistenteService.obtenerAsistentesPorFestival(idFestival, idPromotor);

            request.setAttribute("festival", festival);
            request.setAttribute("asistentes", listaAsistentes);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);
            forwardToJsp("/WEB-INF/jsp/promotor/promotor-festival-asistentes.jsp");
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al listar asistentes festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar asistentes.", e);
        }
    }

    @GET
    @Path("/festivales/{idFestival}/compras")
    @Produces(MediaType.TEXT_HTML)
    public Response listarComprasPorFestival(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/{}/compras", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null) {
            throw new BadRequestException("ID festival no válido.");
        }

        try {
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));
            List<CompraDTO> listaCompras = compraService.obtenerComprasPorFestival(idFestival, idPromotor);

            request.setAttribute("festival", festival);
            request.setAttribute("compras", listaCompras);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);
            forwardToJsp("/WEB-INF/jsp/promotor/promotor-festival-compras.jsp");
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al listar compras festival ID {}: {}", idFestival, e.getMessage(), e);
            setFlashMessage(request, "error", "Error al cargar las compras del festival.");
            return Response.seeOther(buildUri("mostrarDetallesFestival", idFestival)).build();
        }
    }

    @GET
    @Path("/festivales/{idFestival}/pulseras")
    @Produces(MediaType.TEXT_HTML)
    public Response listarPulserasPorFestivalPromotor(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/{}/pulseras", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null) {
            throw new BadRequestException("ID festival no válido.");
        }

        try {
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));
            List<PulseraNFCDTO> listaPulseras = pulseraNFCService.obtenerPulserasPorFestival(idFestival, idPromotor);

            request.setAttribute("festival", festival);
            request.setAttribute("pulseras", listaPulseras);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);
            forwardToJsp("/WEB-INF/jsp/promotor/promotor-festival-pulseras.jsp");
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al listar pulseras festival ID {}: {}", idFestival, e.getMessage(), e);
            setFlashMessage(request, "error", "Error al cargar las pulseras del festival.");
            return Response.seeOther(buildUri("mostrarDetallesFestival", idFestival)).build();
        }
    }

    // --- Cambio de Contraseña Obligatorio ---
    @GET
    @Path("/mostrar-cambio-password")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCambioPassword() throws ServletException, IOException {
        log.debug("GET /promotor/mostrar-cambio-password");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            log.warn("Acceso a mostrar-cambio-password sin sesión válida.");
            return Response.seeOther(URI.create(request.getContextPath() + "/login.jsp?error=session_required")).build();
        }
        Integer userId = (Integer) session.getAttribute("userId");
        log.debug("Mostrando form cambio pass obligatorio para userId: {}", userId);
        mostrarMensajeFlash(request); // Muestra errores de intento previo
        forwardToJsp("/WEB-INF/jsp/cambiar-password-obligatorio.jsp");
        return Response.ok().build();
    }

    @POST
    @Path("/cambiar-password-obligatorio")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response procesarCambioPasswordObligatorio(
            @FormParam("newPassword") String newPassword,
            @FormParam("confirmPassword") String confirmPassword) {

        HttpSession session = request.getSession(false);
        String errorMessage = null;
        URI redirectUri = null;

        if (session == null || session.getAttribute("userId") == null || session.getAttribute("userRole") == null) {
            log.error("Intento de cambiar contraseña obligatorio sin sesión válida.");
            return Response.seeOther(URI.create(request.getContextPath() + "/login.jsp?error=session_expired")).build();
        }
        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        log.info("POST /promotor/cambiar-password-obligatorio para userId: {}", userId);

        if (newPassword == null || newPassword.isEmpty() || !newPassword.equals(confirmPassword)) {
            errorMessage = "Las contraseñas no coinciden o están vacías.";
        } else if (newPassword.length() < 8) {
            errorMessage = "La nueva contraseña debe tener al menos 8 caracteres.";
        }

        if (errorMessage != null) {
            log.warn("Error validación contraseña obligatoria (userId: {}): {}", userId, errorMessage);
            session.setAttribute("passwordChangeError", errorMessage);
            return redirectBackToChangePasswordForm();
        }

        try {
            usuarioService.cambiarPasswordYMarcarActualizada(userId, newPassword);
            log.info("Contraseña obligatoria cambiada para userId: {}", userId);
            session.removeAttribute("passwordChangeError");
            session.setAttribute("mensaje", "Contraseña actualizada. ¡Bienvenido!");
            redirectUri = URI.create(request.getContextPath() + determineDashboardUrlFromRole(userRole));
            return Response.seeOther(redirectUri).build();

        } catch (Exception e) {
            log.error("Error al actualizar contraseña obligatoria userId {}: {}", userId, e.getMessage(), e);
            session.setAttribute("passwordChangeError", "Error al guardar: " + e.getMessage());
            return redirectBackToChangePasswordForm();
        }
    }

    // --- Métodos Auxiliares ---
    /**
     * Verifica acceso PROMOTOR y devuelve su ID. Lanza excepciones JAX-RS.
     */
    private Integer verificarAccesoPromotor(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("Acceso a recurso Promotor sin sesión.");
            throw new NotAuthorizedException("No hay sesión activa.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        if (userId == null || userRole == null) {
            log.warn("Acceso a recurso Promotor con sesión inválida. ID: {}", session.getId());
            session.invalidate();
            throw new NotAuthorizedException("Sesión inválida.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        if (!RolUsuario.PROMOTOR.name().equals(userRole)) {
            log.warn("Usuario ID {} (Rol {}) intentó acceder a recurso Promotor.", userId, userRole);
            throw new ForbiddenException("Acceso denegado. Rol PROMOTOR requerido.");
        }
        log.debug("Acceso permitido para promotor ID: {}", userId);
        return userId;
    }

    /**
     * Realiza forward a un JSP.
     */
    private void forwardToJsp(String jspPath) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
        dispatcher.forward(request, response);
    }

    /**
     * Guarda mensaje flash en sesión.
     */
    private void setFlashMessage(HttpServletRequest request, String type, String message) {
        if (message != null) {
            HttpSession session = request.getSession();
            session.setAttribute(type, message);
            log.trace("Mensaje flash guardado: {}={}", type, message);
        }
    }

    /**
     * Mueve mensajes flash de sesión a request.
     */
    private void mostrarMensajeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String[] keys = {"mensaje", "error", "passwordChangeError", "nuevaEntradaConError", "errorEntrada"};
            String[] requestKeys = {"mensajeExito", "error", "error", "nuevaEntrada", "errorEntrada"}; // Mapeo a claves de request

            for (int i = 0; i < keys.length; i++) {
                Object value = session.getAttribute(keys[i]);
                if (value != null) {
                    request.setAttribute(requestKeys[i], value);
                    session.removeAttribute(keys[i]);
                    log.trace("Mensaje/Atributo flash '{}' movido de sesión a request.", keys[i]);
                }
            }
        }
    }

    /**
     * Construye URI relativa a la base de la API para un método y parámetro ID.
     */
    private URI buildUri(String methodName, Object idParam) {
        UriBuilder builder = uriInfo.getBaseUriBuilder()
                .path(PromotorResource.class)
                .path(PromotorResource.class, methodName);
        if (idParam != null) {
            // Asume que el parámetro se llama 'id' o 'idFestival' o 'idEntrada', etc.
            // Se necesita introspección o un mapa para hacerlo genérico,
            // pero para estos casos específicos podemos hacerlo manualmente:
            String paramName = "id"; // Default
            if ("mostrarDetallesFestival".equals(methodName) || "listarEntradasAsignadas".equals(methodName) || "listarAsistentesPorFestival".equals(methodName) || "listarComprasPorFestival".equals(methodName) || "listarPulserasPorFestivalPromotor".equals(methodName)) {
                paramName = "idFestival";
            } else if ("mostrarFormularioEditarEntrada".equals(methodName) || "actualizarEntrada".equals(methodName) || "eliminarEntrada".equals(methodName)) {
                paramName = "idEntrada";
            } else if ("nominarEntrada".equals(methodName) || "cancelarEntrada".equals(methodName) || "asociarPulseraPromotor".equals(methodName)) {
                paramName = "idEntradaAsignada";
            }
            return builder.resolveTemplate(paramName, idParam).build();
        } else {
            return builder.build();
        }
    }

    /**
     * Forward a form de edición de festival con error.
     */
    private void forwardToPromotorFestivalEditFormWithError(FestivalDTO dto, Integer idPromotor, boolean esEdicion, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.setAttribute("festival", dto);
        request.setAttribute("esNuevo", !esEdicion);
        request.setAttribute("idPromotorAutenticado", idPromotor);
        forwardToJsp("/WEB-INF/jsp/promotor/promotor-festival-editar.jsp");
    }

    /**
     * Redirige al form de cambio de contraseña obligatorio.
     */
    private Response redirectBackToChangePasswordForm() {
        try {
            URI formUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarFormularioCambioPassword")
                    .build();
            log.debug("Redirigiendo de vuelta a form cambio pass: {}", formUri);
            return Response.seeOther(formUri).build();
        } catch (Exception e) {
            log.error("Error creando URI para redirect a cambiar pass: {}", e.getMessage(), e);
            return Response.serverError().entity("Error interno redirect.").build();
        }
    }

    /**
     * Determina URL del dashboard según rol.
     */
    private String determineDashboardUrlFromRole(String roleName) {
        if (RolUsuario.PROMOTOR.name().equalsIgnoreCase(roleName)) {
            return "/api/promotor/festivales";
        }
        // Añadir otros roles si es necesario
        log.warn("Rol inesperado '{}' al determinar URL dashboard.", roleName);
        return "/login.jsp?error=unexpected_role"; // Fallback
    }

}
