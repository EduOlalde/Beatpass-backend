package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO; 
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraResponseDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import com.daw2edudiego.beatpasstfg.util.QRCodeUtil;

// Stripe Imports
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la interfaz {@link VentaService}. Orquesta el proceso
 * completo de registro de una venta de entradas, asegurando la atomicidad de la
 * operación a través de transacciones JPA, manejando la lógica de negocio
 * (validación, stock) y verificando pagos con Stripe antes de confirmar la
 * venta.
 *
 * @see VentaService
 * @see CompraRepository
 * @see EntradaRepository
 * @see EntradaAsignadaRepository
 * @author Eduardo Olalde
 */
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    // Repositorios
    private final AsistenteRepository asistenteRepository;
    private final EntradaRepository entradaRepository;
    private final CompraRepository compraRepository;
    private final CompraEntradaRepository compraEntradaRepository;
    private final EntradaAsignadaRepository entradaAsignadaRepository;

    // Constante para la moneda esperada
    private static final String EXPECTED_CURRENCY = "eur";

    /**
     * Constructor que inicializa los repositorios necesarios.
     */
    public VentaServiceImpl() {
        this.asistenteRepository = new AsistenteRepositoryImpl();
        this.entradaRepository = new EntradaRepositoryImpl();
        this.compraRepository = new CompraRepositoryImpl();
        this.compraEntradaRepository = new CompraEntradaRepositoryImpl();
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Usar
     * {@link #confirmarVentaConPago(Integer, Integer, int, String)} para ventas
     * con pago online.
     */
    @Override
    @Deprecated
    public void registrarVenta(Integer idAsistente, Integer idEntrada, int cantidad) {
        log.warn("Llamada a método obsoleto registrarVenta (sin pago) - Asistente ID: {}, Entrada ID: {}, Cantidad: {}",
                idAsistente, idEntrada, cantidad);
        throw new UnsupportedOperationException("Este método está obsoleto. Use confirmarVentaConPago.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompraDTO confirmarVentaConPago(Integer idAsistente, Integer idEntrada, int cantidad, String paymentIntentId)
            throws AsistenteNotFoundException, EntradaNotFoundException, FestivalNoPublicadoException,
            StockInsuficienteException, PagoInvalidoException, IllegalArgumentException {

        log.info("Service: Iniciando confirmación de venta con pago - Asistente ID: {}, Entrada ID: {}, Cant: {}, PI: {}",
                idAsistente, idEntrada, cantidad, paymentIntentId);

        // Validación inicial de parámetros
        if (idAsistente == null || idEntrada == null) {
            throw new IllegalArgumentException("ID de asistente y ID de entrada son requeridos.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de entradas debe ser mayor que cero.");
        }
        if (paymentIntentId == null || !paymentIntentId.startsWith("pi_")) {
            throw new IllegalArgumentException("ID de PaymentIntent de Stripe inválido.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        Entrada entrada;
        List<EntradaAsignada> entradasGeneradasPersistidas = new ArrayList<>(); // Para guardar las entidades generadas

        try {
            // --- Paso 1: Validaciones previas y cálculo del total esperado (sin TX aún) ---
            em = JPAUtil.createEntityManager();
            Asistente asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));
            entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));
            Festival festival = entrada.getFestival();
            if (festival == null) {
                throw new IllegalStateException("Error interno: La entrada no está asociada a ningún festival.");
            }
            if (festival.getEstado() != EstadoFestival.PUBLICADO) {
                throw new FestivalNoPublicadoException("No se pueden comprar entradas para el festival '"
                        + festival.getNombre() + "' (estado: " + festival.getEstado() + ").");
            }
            BigDecimal totalEsperadoDecimal = entrada.getPrecio().multiply(new BigDecimal(cantidad));
            long totalEsperadoCentimos = totalEsperadoDecimal.multiply(new BigDecimal(100)).longValueExact();
            closeEntityManager(em); // Cerrar EM inicial

            // --- Paso 2: Verificación del Pago con Stripe ---
            log.debug("Verificando PaymentIntent de Stripe: {}", paymentIntentId);
            PaymentIntent paymentIntent;
            try {
                PaymentIntentRetrieveParams params = PaymentIntentRetrieveParams.builder().build();
                paymentIntent = PaymentIntent.retrieve(paymentIntentId, params, null);
                if (!"succeeded".equals(paymentIntent.getStatus())) {
                    throw new PagoInvalidoException("El pago no se ha completado correctamente (Estado Stripe: " + paymentIntent.getStatus() + ")");
                }
                if (paymentIntent.getAmount() == null || paymentIntent.getAmount() != totalEsperadoCentimos) {
                    throw new PagoInvalidoException("El monto del pago (" + paymentIntent.getAmount() + ") no coincide con el total esperado (" + totalEsperadoCentimos + ").");
                }
                if (!EXPECTED_CURRENCY.equalsIgnoreCase(paymentIntent.getCurrency())) {
                    throw new PagoInvalidoException("La moneda del pago (" + paymentIntent.getCurrency() + ") no coincide con la esperada (" + EXPECTED_CURRENCY + ").");
                }
                log.info("Verificación de Stripe PaymentIntent {} exitosa.", paymentIntentId);
            } catch (StripeException e) {
                throw new PagoInvalidoException("Error al verificar el estado del pago con la pasarela: " + e.getMessage(), e);
            }

            // --- Paso 3: Proceder con la lógica de negocio (Transacción JPA) ---
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));
            entrada = em.find(Entrada.class, idEntrada, LockModeType.PESSIMISTIC_WRITE);
            if (entrada == null) {
                throw new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada);
            }
            if (entrada.getStock() == null || entrada.getStock() < cantidad) {
                throw new StockInsuficienteException("Lo sentimos, el stock (" + entrada.getStock() + ") para el tipo de entrada '"
                        + entrada.getTipo() + "' se agotó mientras procesabas el pago.");
            }

            Compra compra = new Compra();
            compra.setAsistente(asistente);
            compra.setTotal(totalEsperadoDecimal);
            compra.setStripePaymentIntentId(paymentIntentId);
            compra.setEstadoPago("PAGADO");
            if (paymentIntent.getCreated() != null) {
                Instant instant = Instant.ofEpochSecond(paymentIntent.getCreated());
                compra.setFechaPagoConfirmado(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
            }
            compra = compraRepository.save(em, compra);

            CompraEntrada compraEntrada = new CompraEntrada();
            compraEntrada.setCompra(compra);
            compraEntrada.setEntrada(entrada);
            compraEntrada.setCantidad(cantidad);
            compraEntrada.setPrecioUnitario(entrada.getPrecio());
            compraEntrada = compraEntradaRepository.save(em, compraEntrada);

            // Generar Entradas Asignadas y guardarlas en la lista
            for (int i = 0; i < cantidad; i++) {
                EntradaAsignada entradaAsignada = new EntradaAsignada();
                entradaAsignada.setCompraEntrada(compraEntrada);
                entradaAsignada.setEstado(EstadoEntradaAsignada.ACTIVA);
                String qrContent = QRCodeUtil.generarContenidoQrUnico();
                entradaAsignada.setCodigoQr(qrContent);
                entradaAsignada = entradaAsignadaRepository.save(em, entradaAsignada);
                entradasGeneradasPersistidas.add(entradaAsignada); // <-- Guardar la entidad generada
                log.trace("Generada y guardada EntradaAsignada ID: {} con QR final: ...{}",
                        entradaAsignada.getIdEntradaAsignada(),
                        qrContent.substring(Math.max(0, qrContent.length() - 6)));
            }
            log.debug("Generadas {} entradas asignadas para CompraEntrada ID: {}", cantidad, compraEntrada.getIdCompraEntrada());

            int nuevoStock = entrada.getStock() - cantidad;
            entrada.setStock(nuevoStock);
            entradaRepository.save(em, entrada);
            log.info("Stock actualizado para Entrada ID {}. Nuevo stock: {}", idEntrada, nuevoStock);

            tx.commit();
            log.info("Venta confirmada con pago exitoso y transacción completada. Compra ID: {}, PI: {}",
                    compra.getIdCompra(), paymentIntentId);

            // Mapear Compra a CompraDTO, incluyendo las entradas generadas
            CompraDTO compraDTO = mapCompraToDTO(compra, entradasGeneradasPersistidas); // <-- Pasar lista de entidades
            return compraDTO;

        } catch (Exception e) {
            handleException(e, tx, "confirmar venta con pago para PI " + paymentIntentId);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IniciarCompraResponseDTO iniciarProcesoPago(Integer idEntrada, int cantidad)
            throws EntradaNotFoundException, FestivalNoPublicadoException, IllegalArgumentException {

        log.info("Service: Iniciando proceso de pago - Entrada ID: {}, Cantidad: {}", idEntrada, cantidad);

        if (idEntrada == null) {
            throw new IllegalArgumentException("ID de entrada es requerido.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de entradas debe ser mayor que cero.");
        }

        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));
            Festival festival = entrada.getFestival();
            if (festival == null) {
                throw new IllegalStateException("Error interno: La entrada no está asociada a ningún festival.");
            }
            if (festival.getEstado() != EstadoFestival.PUBLICADO) {
                throw new FestivalNoPublicadoException("No se pueden comprar entradas para el festival '"
                        + festival.getNombre() + "' (estado: " + festival.getEstado() + ").");
            }

            BigDecimal totalDecimal = entrada.getPrecio().multiply(new BigDecimal(cantidad));
            long totalCentimos = totalDecimal.multiply(new BigDecimal(100)).longValueExact();
            log.debug("Total calculado para {} entradas de tipo '{}': {} {} ({} céntimos)",
                    cantidad, entrada.getTipo(), totalDecimal, EXPECTED_CURRENCY.toUpperCase(), totalCentimos);

            log.debug("Creando PaymentIntent en Stripe...");
            PaymentIntent paymentIntent;
            try {
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(totalCentimos)
                        .setCurrency(EXPECTED_CURRENCY)
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                        )
                        .build();
                paymentIntent = PaymentIntent.create(params);
                log.info("PaymentIntent de Stripe creado con ID: {}", paymentIntent.getId());
            } catch (StripeException e) {
                throw new RuntimeException("Error al iniciar el proceso de pago con la pasarela: " + e.getMessage(), e);
            }
            return new IniciarCompraResponseDTO(paymentIntent.getClientSecret());
        } catch (Exception e) {
            log.error("Error en iniciarProcesoPago para entrada ID {}: {}", idEntrada, e.getMessage(), e);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda (Helpers) ---
    /**
     * Manejador genérico de excepciones.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("Rollback de transacción de '{}' realizado.", action);
            } catch (Exception rbEx) {
                log.error("¡Error CRÍTICO durante el rollback de '{}'!: {}", action, rbEx.getMessage(), rbEx);
            }
        }
    }

    /**
     * Cierra el EntityManager si está abierto.
     */
    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            try {
                em.close();
            } catch (Exception e) {
                log.error("Error al cerrar EntityManager: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Mapea excepciones técnicas a excepciones de negocio o RuntimeException.
     */
    private RuntimeException mapException(Exception e) {
        if (e instanceof AsistenteNotFoundException || e instanceof EntradaNotFoundException
                || e instanceof FestivalNotFoundException || e instanceof FestivalNoPublicadoException
                || e instanceof StockInsuficienteException || e instanceof PagoInvalidoException
                || e instanceof IllegalArgumentException || e instanceof SecurityException
                || e instanceof IllegalStateException || e instanceof PersistenceException
                || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        log.error("Encontrada excepción chequeada inesperada, envolviendo en RuntimeException: {}", e.getClass().getName());
        return new RuntimeException("Error inesperado en la capa de servicio Venta: " + e.getMessage(), e);
    }

    /**
     * Mapea una entidad {@link Compra} y la lista de sus
     * {@link EntradaAsignada} generadas a su correspondiente {@link CompraDTO}.
     *
     * @param compra La entidad {@link Compra} a mapear. No debe ser
     * {@code null}.
     * @param entradasGeneradas La lista de entidades {@link EntradaAsignada}
     * que se acaban de generar para esta compra.
     * @return El objeto {@link CompraDTO} poblado.
     */
    private CompraDTO mapCompraToDTO(Compra compra, List<EntradaAsignada> entradasGeneradas) {
        if (compra == null) {
            log.warn("Intento de mapear una entidad Compra nula a DTO.");
            return null;
        }
        CompraDTO dto = new CompraDTO();
        dto.setIdCompra(compra.getIdCompra());
        dto.setFechaCompra(compra.getFechaCompra());
        dto.setTotal(compra.getTotal());
        dto.setStripePaymentIntentId(compra.getStripePaymentIntentId());
        dto.setEstadoPago(compra.getEstadoPago());
        dto.setFechaPagoConfirmado(compra.getFechaPagoConfirmado());

        if (compra.getAsistente() != null) {
            dto.setIdAsistente(compra.getAsistente().getIdAsistente());
            dto.setEmailAsistente(compra.getAsistente().getEmail());
            dto.setNombreAsistente(compra.getAsistente().getNombre());
        } else {
            log.warn("La Compra ID {} no tiene un Asistente asociado.", compra.getIdCompra());
        }

        // Mapear las entradas generadas (pasadas como argumento) a DTOs
        if (entradasGeneradas != null && !entradasGeneradas.isEmpty()) {
            List<EntradaAsignadaDTO> entradasDTO = entradasGeneradas.stream()
                    .map(this::mapEntradaAsignadaToDTO) // Usar método helper para mapear cada entrada
                    .collect(Collectors.toList());
            dto.setEntradasGeneradas(entradasDTO); // Establecer la lista en el DTO de Compra

            // Opcional: generar también el resumen si aún se usa en algún sitio
            try {
                if (compra.getDetallesCompra() != null && !compra.getDetallesCompra().isEmpty()) {
                    List<String> resumen = compra.getDetallesCompra().stream()
                            .map(detalle -> {
                                String tipoEntrada = (detalle.getEntrada() != null) ? detalle.getEntrada().getTipo() : "Desconocido";
                                return detalle.getCantidad() + " x " + tipoEntrada;
                            })
                            .collect(Collectors.toList());
                    dto.setResumenEntradas(resumen);
                }
            } catch (org.hibernate.LazyInitializationException e) {
                log.warn("LazyInitializationException al acceder a detallesCompra para Compra ID {}. No se incluirá resumen.", compra.getIdCompra());
                dto.setResumenEntradas(new ArrayList<>());
            }
        } else {
            log.warn("No se proporcionaron entradas generadas para mapear en Compra ID {}", compra.getIdCompra());
            dto.setEntradasGeneradas(new ArrayList<>()); // Asegurar lista vacía
        }
        return dto;
    }

    /**
     * Mapea una entidad {@link EntradaAsignada} a su DTO
     * {@link EntradaAsignadaDTO}, asegurando la compatibilidad con el DTO
     * proporcionado.
     *
     * @param entidad La entidad EntradaAsignada.
     * @return El DTO mapeado.
     */
    private EntradaAsignadaDTO mapEntradaAsignadaToDTO(EntradaAsignada entidad) {
        if (entidad == null) {
            return null;
        }
        EntradaAsignadaDTO dto = new EntradaAsignadaDTO();
        dto.setIdEntradaAsignada(entidad.getIdEntradaAsignada());
        dto.setCodigoQr(entidad.getCodigoQr());
        dto.setEstado(entidad.getEstado());
        dto.setFechaAsignacion(entidad.getFechaAsignacion());
        dto.setFechaUso(entidad.getFechaUso());

        if (entidad.getAsistente() != null) {
            dto.setIdAsistente(entidad.getAsistente().getIdAsistente());
            dto.setNombreAsistente(entidad.getAsistente().getNombre());
            dto.setEmailAsistente(entidad.getAsistente().getEmail());
        }

        // Obtener info de la entrada original, festival y compra a través de compraEntrada
        if (entidad.getCompraEntrada() != null) {
            // Establecer ID de CompraEntrada en el DTO
            dto.setIdCompraEntrada(entidad.getCompraEntrada().getIdCompraEntrada()); // <-- Añadido según DTO

            if (entidad.getCompraEntrada().getEntrada() != null) {
                Entrada entradaOriginal = entidad.getCompraEntrada().getEntrada();
                dto.setIdEntradaOriginal(entradaOriginal.getIdEntrada());
                dto.setTipoEntradaOriginal(entradaOriginal.getTipo());
                // Añadir ID y nombre del festival desde la Entrada original
                if (entradaOriginal.getFestival() != null) {
                    dto.setIdFestival(entradaOriginal.getFestival().getIdFestival());
                    dto.setNombreFestival(entradaOriginal.getFestival().getNombre());
                }
            }
            // El ID de la Compra NO está directamente en EntradaAsignadaDTO,
            // se obtiene del CompraDTO padre si es necesario.
            // Si se necesitara aquí, habría que añadirlo al DTO y mapearlo desde entidad.getCompraEntrada().getCompra().getIdCompra()
        }

        // Mapear información de la pulsera si la relación existiera en la entidad EntradaAsignada
        // if (entidad.getPulseraNFC() != null) { // Asumiendo que existe getPulseraNFC()
        //     dto.setIdPulseraAsociada(entidad.getPulseraNFC().getIdPulsera());
        //     dto.setCodigoUidPulsera(entidad.getPulseraNFC().getCodigoUid());
        // }
        // El campo qrCodeImageDataUrl se genera en el frontend
        return dto;
    }
}
