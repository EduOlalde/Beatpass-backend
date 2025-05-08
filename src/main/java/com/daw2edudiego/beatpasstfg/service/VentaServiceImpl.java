package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraResponseDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import com.daw2edudiego.beatpasstfg.util.QRCodeUtil;

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
 * Implementación de VentaService.
 */
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    private final AsistenteRepository asistenteRepository;
    private final EntradaRepository entradaRepository;
    private final CompraRepository compraRepository;
    private final CompraEntradaRepository compraEntradaRepository;
    private final EntradaAsignadaRepository entradaAsignadaRepository;

    private static final String EXPECTED_CURRENCY = "eur";

    public VentaServiceImpl() {
        this.asistenteRepository = new AsistenteRepositoryImpl();
        this.entradaRepository = new EntradaRepositoryImpl();
        this.compraRepository = new CompraRepositoryImpl();
        this.compraEntradaRepository = new CompraEntradaRepositoryImpl();
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
    }

    @Override
    public CompraDTO confirmarVentaConPago(Integer idAsistente, Integer idEntrada, int cantidad, String paymentIntentId)
            throws AsistenteNotFoundException, EntradaNotFoundException, FestivalNoPublicadoException,
            StockInsuficienteException, PagoInvalidoException, IllegalArgumentException {

        log.info("Service: Iniciando confirmación de venta con pago - Asistente ID: {}, Entrada ID: {}, Cant: {}, PI: {}",
                idAsistente, idEntrada, cantidad, paymentIntentId);

        validarParametrosConfirmacion(idAsistente, idEntrada, cantidad, paymentIntentId);

        EntityManager em = null;
        EntityTransaction tx = null;
        Entrada entrada;
        List<EntradaAsignada> entradasGeneradasPersistidas = new ArrayList<>();

        try {
            // Validaciones previas y cálculo del total esperado (sin TX)
            em = JPAUtil.createEntityManager();
            asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));
            entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));
            Festival festival = entrada.getFestival();
            if (festival == null) {
                throw new IllegalStateException("Entrada sin festival asociado.");
            }
            if (festival.getEstado() != EstadoFestival.PUBLICADO) {
                throw new FestivalNoPublicadoException("Festival '" + festival.getNombre() + "' no está publicado.");
            }
            BigDecimal totalEsperadoDecimal = entrada.getPrecio().multiply(new BigDecimal(cantidad));
            long totalEsperadoCentimos = totalEsperadoDecimal.multiply(new BigDecimal(100)).longValueExact();
            closeEntityManager(em); // Cerrar EM inicial

            // Verificación del Pago con Stripe
            PaymentIntent paymentIntent = verificarPagoStripe(paymentIntentId, totalEsperadoCentimos);

            // Lógica de negocio (Transacción JPA)
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Asistente asistente = asistenteRepository.findById(em, idAsistente) // Re-obtener dentro de TX
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));
            entrada = em.find(Entrada.class, idEntrada, LockModeType.PESSIMISTIC_WRITE); // Bloquear
            if (entrada == null) {
                throw new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada);
            }
            if (entrada.getStock() == null || entrada.getStock() < cantidad) {
                throw new StockInsuficienteException("Stock (" + entrada.getStock() + ") insuficiente para entrada '" + entrada.getTipo() + "'.");
            }

            Compra compra = crearYGuardarCompra(em, asistente, totalEsperadoDecimal, paymentIntent);
            CompraEntrada compraEntrada = crearYGuardarCompraEntrada(em, compra, entrada, cantidad);
            generarYGuardarEntradasAsignadas(em, compraEntrada, cantidad, entradasGeneradasPersistidas);
            actualizarStockEntrada(em, entrada, cantidad);

            tx.commit();
            log.info("Venta confirmada con pago y TX completada. Compra ID: {}, PI: {}", compra.getIdCompra(), paymentIntentId);

            return mapCompraToDTO(compra, entradasGeneradasPersistidas);

        } catch (Exception e) {
            handleException(e, tx, "confirmar venta con pago para PI " + paymentIntentId);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public IniciarCompraResponseDTO iniciarProcesoPago(Integer idEntrada, int cantidad)
            throws EntradaNotFoundException, FestivalNoPublicadoException, IllegalArgumentException {

        log.info("Service: Iniciando proceso de pago - Entrada ID: {}, Cantidad: {}", idEntrada, cantidad);
        if (idEntrada == null || cantidad <= 0) {
            throw new IllegalArgumentException("ID entrada y cantidad > 0 son requeridos.");
        }

        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));
            validarFestivalParaCompra(entrada.getFestival());

            BigDecimal totalDecimal = entrada.getPrecio().multiply(new BigDecimal(cantidad));
            long totalCentimos = totalDecimal.multiply(new BigDecimal(100)).longValueExact();
            log.debug("Total calculado para {} entradas tipo '{}': {} {} ({} céntimos)",
                    cantidad, entrada.getTipo(), totalDecimal, EXPECTED_CURRENCY.toUpperCase(), totalCentimos);

            PaymentIntent paymentIntent = crearPaymentIntentStripe(totalCentimos);
            return new IniciarCompraResponseDTO(paymentIntent.getClientSecret());

        } catch (Exception e) {
            log.error("Error en iniciarProcesoPago para entrada ID {}: {}", idEntrada, e.getMessage(), e);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda ---
    /**
     * Valida parámetros de entrada para confirmarVentaConPago.
     */
    private void validarParametrosConfirmacion(Integer idAsistente, Integer idEntrada, int cantidad, String paymentIntentId) {
        if (idAsistente == null || idEntrada == null) {
            throw new IllegalArgumentException("ID asistente y entrada requeridos.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser > 0.");
        }
        if (paymentIntentId == null || !paymentIntentId.startsWith("pi_")) {
            throw new IllegalArgumentException("ID PaymentIntent inválido.");
        }
    }

    /**
     * Verifica el estado y monto del PaymentIntent en Stripe.
     */
    private PaymentIntent verificarPagoStripe(String paymentIntentId, long totalEsperadoCentimos) throws PagoInvalidoException {
        log.debug("Verificando PaymentIntent de Stripe: {}", paymentIntentId);
        try {
            PaymentIntentRetrieveParams params = PaymentIntentRetrieveParams.builder().build();
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId, params, null);
            if (!"succeeded".equals(paymentIntent.getStatus())) {
                throw new PagoInvalidoException("Pago no completado (Estado Stripe: " + paymentIntent.getStatus() + ")");
            }
            if (paymentIntent.getAmount() == null || paymentIntent.getAmount() != totalEsperadoCentimos) {
                throw new PagoInvalidoException("Monto del pago (" + paymentIntent.getAmount() + ") no coincide con esperado (" + totalEsperadoCentimos + ").");
            }
            if (!EXPECTED_CURRENCY.equalsIgnoreCase(paymentIntent.getCurrency())) {
                throw new PagoInvalidoException("Moneda del pago (" + paymentIntent.getCurrency() + ") no coincide con esperada (" + EXPECTED_CURRENCY + ").");
            }
            log.info("Verificación Stripe PaymentIntent {} exitosa.", paymentIntentId);
            return paymentIntent;
        } catch (StripeException e) {
            throw new PagoInvalidoException("Error al verificar pago: " + e.getMessage(), e);
        }
    }

    /**
     * Crea y guarda la entidad Compra.
     */
    private Compra crearYGuardarCompra(EntityManager em, Asistente asistente, BigDecimal total, PaymentIntent pi) {
        Compra compra = new Compra();
        compra.setAsistente(asistente);
        compra.setTotal(total);
        compra.setStripePaymentIntentId(pi.getId());
        compra.setEstadoPago("PAGADO");
        if (pi.getCreated() != null) {
            compra.setFechaPagoConfirmado(LocalDateTime.ofInstant(Instant.ofEpochSecond(pi.getCreated()), ZoneId.systemDefault()));
        }
        return compraRepository.save(em, compra);
    }

    /**
     * Crea y guarda la entidad CompraEntrada.
     */
    private CompraEntrada crearYGuardarCompraEntrada(EntityManager em, Compra compra, Entrada entrada, int cantidad) {
        CompraEntrada compraEntrada = new CompraEntrada();
        compraEntrada.setCompra(compra);
        compraEntrada.setEntrada(entrada);
        compraEntrada.setCantidad(cantidad);
        compraEntrada.setPrecioUnitario(entrada.getPrecio());
        return compraEntradaRepository.save(em, compraEntrada);
    }

    /**
     * Genera y guarda las entidades EntradaAsignada.
     */
    private void generarYGuardarEntradasAsignadas(EntityManager em, CompraEntrada ce, int cantidad, List<EntradaAsignada> listaPersistida) {
        for (int i = 0; i < cantidad; i++) {
            EntradaAsignada ea = new EntradaAsignada();
            ea.setCompraEntrada(ce);
            ea.setEstado(EstadoEntradaAsignada.ACTIVA);
            ea.setCodigoQr(QRCodeUtil.generarContenidoQrUnico());
            listaPersistida.add(entradaAsignadaRepository.save(em, ea));
        }
        log.debug("Generadas {} entradas asignadas para CompraEntrada ID: {}", cantidad, ce.getIdCompraEntrada());
    }

    /**
     * Actualiza el stock de la Entrada original.
     */
    private void actualizarStockEntrada(EntityManager em, Entrada entrada, int cantidad) {
        int nuevoStock = entrada.getStock() - cantidad;
        entrada.setStock(nuevoStock);
        entradaRepository.save(em, entrada);
        log.info("Stock actualizado para Entrada ID {}. Nuevo stock: {}", entrada.getIdEntrada(), nuevoStock);
    }

    /**
     * Valida si el festival asociado a una entrada permite compras.
     */
    private void validarFestivalParaCompra(Festival festival) {
        if (festival == null) {
            throw new IllegalStateException("Entrada sin festival asociado.");
        }
        if (festival.getEstado() != EstadoFestival.PUBLICADO) {
            throw new FestivalNoPublicadoException("Festival '" + festival.getNombre() + "' no está publicado.");
        }
    }

    /**
     * Crea un PaymentIntent en Stripe.
     */
    private PaymentIntent crearPaymentIntentStripe(long totalCentimos) {
        log.debug("Creando PaymentIntent en Stripe por {} céntimos...", totalCentimos);
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(totalCentimos)
                    .setCurrency(EXPECTED_CURRENCY)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                    )
                    .build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            log.info("PaymentIntent Stripe creado con ID: {}", paymentIntent.getId());
            return paymentIntent;
        } catch (StripeException e) {
            throw new RuntimeException("Error al iniciar pago con Stripe: " + e.getMessage(), e);
        }
    }

    /**
     * Manejador genérico de excepciones.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

    /**
     * Realiza rollback si la transacción está activa.
     */
    private void rollbackTransaction(EntityTransaction tx, String action) {
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
     * Cierra el EntityManager.
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
     * Mapea excepciones técnicas a de negocio o Runtime.
     */
    private RuntimeException mapException(Exception e) {
        if (e instanceof AsistenteNotFoundException || e instanceof EntradaNotFoundException || e instanceof FestivalNotFoundException
                || e instanceof FestivalNoPublicadoException || e instanceof StockInsuficienteException || e instanceof PagoInvalidoException
                || e instanceof IllegalArgumentException || e instanceof SecurityException || e instanceof IllegalStateException
                || e instanceof PersistenceException || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio Venta: " + e.getMessage(), e);
    }

    /**
     * Mapea entidad Compra y sus entradas generadas a CompraDTO.
     */
    private CompraDTO mapCompraToDTO(Compra compra, List<EntradaAsignada> entradasGeneradas) {
        if (compra == null) {
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
        }

        if (entradasGeneradas != null && !entradasGeneradas.isEmpty()) {
            dto.setEntradasGeneradas(entradasGeneradas.stream()
                    .map(this::mapEntradaAsignadaToDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setEntradasGeneradas(new ArrayList<>());
        }

        try {
            if (compra.getDetallesCompra() != null && !compra.getDetallesCompra().isEmpty()) {
                dto.setResumenEntradas(compra.getDetallesCompra().stream()
                        .map(detalle -> detalle.getCantidad() + " x " + (detalle.getEntrada() != null ? detalle.getEntrada().getTipo() : "?"))
                        .collect(Collectors.toList()));
            }
        } catch (org.hibernate.LazyInitializationException e) {
            log.warn("LazyInitializationException al acceder a detallesCompra para Compra ID {}. No se incluirá resumen.", compra.getIdCompra());
            dto.setResumenEntradas(new ArrayList<>());
        }
        return dto;
    }

    /**
     * Mapea entidad EntradaAsignada a DTO.
     */
    private EntradaAsignadaDTO mapEntradaAsignadaToDTO(EntradaAsignada ea) {
        if (ea == null) {
            return null;
        }
        EntradaAsignadaDTO dto = new EntradaAsignadaDTO();
        dto.setIdEntradaAsignada(ea.getIdEntradaAsignada());
        dto.setCodigoQr(ea.getCodigoQr());
        dto.setEstado(ea.getEstado());
        dto.setFechaAsignacion(ea.getFechaAsignacion());
        dto.setFechaUso(ea.getFechaUso());

        if (ea.getCompraEntrada() != null) {
            dto.setIdCompraEntrada(ea.getCompraEntrada().getIdCompraEntrada());
            if (ea.getCompraEntrada().getEntrada() != null) {
                Entrada entradaOriginal = ea.getCompraEntrada().getEntrada();
                dto.setIdEntradaOriginal(entradaOriginal.getIdEntrada());
                dto.setTipoEntradaOriginal(entradaOriginal.getTipo());
                if (entradaOriginal.getFestival() != null) {
                    dto.setIdFestival(entradaOriginal.getFestival().getIdFestival());
                    dto.setNombreFestival(entradaOriginal.getFestival().getNombre());
                }
            }
        }
        if (ea.getAsistente() != null) {
            dto.setIdAsistente(ea.getAsistente().getIdAsistente());
            dto.setNombreAsistente(ea.getAsistente().getNombre());
            dto.setEmailAsistente(ea.getAsistente().getEmail());
        }
        if (ea.getPulseraAsociada() != null) {
            dto.setIdPulseraAsociada(ea.getPulseraAsociada().getIdPulsera());
            dto.setCodigoUidPulsera(ea.getPulseraAsociada().getCodigoUid());
        }
        // El campo qrCodeImageDataUrl se genera en el frontend o al mapear en el servicio
        if (ea.getCodigoQr() != null && !ea.getCodigoQr().isBlank()) {
            String imageDataUrl = QRCodeUtil.generarQrComoBase64(ea.getCodigoQr(), 100, 100);
            if (imageDataUrl != null) {
                dto.setQrCodeImageDataUrl(imageDataUrl);
            } else {
                log.warn("No se pudo generar imagen QR para entrada {}", ea.getIdEntradaAsignada());
            }
        }
        return dto;
    }
}
