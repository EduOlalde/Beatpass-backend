package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
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

    private final CompradorService compradorService;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final CompraRepository compraRepository;
    private final CompraEntradaRepository compraEntradaRepository;
    private final EntradaRepository entradaRepository;
    private final EmailService emailService;

    private static final String EXPECTED_CURRENCY = "eur";

    public VentaServiceImpl() {
        this.compradorService = new CompradorServiceImpl();
        this.tipoEntradaRepository = new TipoEntradaRepositoryImpl();
        this.compraRepository = new CompraRepositoryImpl();
        this.compraEntradaRepository = new CompraEntradaRepositoryImpl();
        this.entradaRepository = new EntradaRepositoryImpl();
        this.emailService = new EmailServiceImpl();
    }

    @Override
    public CompraDTO confirmarVentaConPago(String emailComprador, String nombreComprador, String telefonoComprador, Integer idTipoEntrada, int cantidad, String paymentIntentId)
            throws TipoEntradaNotFoundException, FestivalNoPublicadoException,
            StockInsuficienteException, PagoInvalidoException, IllegalArgumentException {

        log.info("Service: Iniciando confirmación de venta - Comprador Email: {}, Entrada ID: {}, Cant: {}, PI: {}",
                emailComprador, idTipoEntrada, cantidad, paymentIntentId);

        validarParametrosConfirmacion(emailComprador, nombreComprador, idTipoEntrada, cantidad, paymentIntentId);

        EntityManager em = null;
        EntityTransaction tx = null;

        Comprador compradorParaEmail;
        Festival festivalParaEmail;
        Compra compraPersistidaParaEmail = null;
        List<EntradaDTO> entradasCompradasDTOsParaEmail = new ArrayList<>();

        try {
            // 1. Validaciones previas y obtención/creación de comprador
            compradorParaEmail = compradorService.obtenerOcrearCompradorPorEmail(emailComprador, nombreComprador, telefonoComprador);

            em = JPAUtil.createEntityManager();
            TipoEntrada tipoEntradaValidada = tipoEntradaRepository.findById(em, idTipoEntrada)
                    .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idTipoEntrada));

            festivalParaEmail = tipoEntradaValidada.getFestival();
            if (festivalParaEmail == null) {
                throw new IllegalStateException("Entrada ID " + idTipoEntrada + " sin festival asociado.");
            }
            if (festivalParaEmail.getEstado() != EstadoFestival.PUBLICADO) {
                throw new FestivalNoPublicadoException("Festival '" + festivalParaEmail.getNombre() + "' no está publicado.");
            }

            BigDecimal totalEsperadoDecimal = tipoEntradaValidada.getPrecio().multiply(new BigDecimal(cantidad));
            long totalEsperadoCentimos = totalEsperadoDecimal.multiply(new BigDecimal(100)).longValueExact();
            closeEntityManager(em);

            // 2. Verificación del Pago con Stripe
            PaymentIntent paymentIntent = verificarPagoStripe(paymentIntentId, totalEsperadoCentimos);

            // 3. Lógica de negocio principal (Transacción JPA)
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Comprador compradorEnTx = em.find(Comprador.class, compradorParaEmail.getIdComprador());

            TipoEntrada tipoEntradaEnTx = em.find(TipoEntrada.class, idTipoEntrada, LockModeType.PESSIMISTIC_WRITE);
            if (tipoEntradaEnTx == null) {
                throw new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idTipoEntrada + " dentro de TX.");
            }
            if (tipoEntradaEnTx.getStock() == null || tipoEntradaEnTx.getStock() < cantidad) {
                throw new StockInsuficienteException("Stock (" + tipoEntradaEnTx.getStock() + ") insuficiente para entrada '" + tipoEntradaEnTx.getTipo() + "'.");
            }

            compraPersistidaParaEmail = crearYGuardarCompra(em, compradorEnTx, totalEsperadoDecimal, paymentIntent);
            CompraEntrada compraEntrada = crearYGuardarCompraEntrada(em, compraPersistidaParaEmail, tipoEntradaEnTx, cantidad);

            List<Entrada> entradasGeneradasPersistidas = new ArrayList<>();
            generarYGuardarEntradasAsignadas(em, compraEntrada, cantidad, entradasGeneradasPersistidas);
            actualizarStockEntrada(em, tipoEntradaEnTx, cantidad);

            for (Entrada ea : entradasGeneradasPersistidas) {
                entradasCompradasDTOsParaEmail.add(mapEntradaToDTO(ea));
            }
            tx.commit();
            log.info("Venta confirmada y TX completada. Compra ID: {}, PI: {}", compraPersistidaParaEmail.getIdCompra(), paymentIntentId);

            // --- Envío de Email ---
            emailService.enviarEmailEntradasCompradas(
                    compradorParaEmail.getEmail(),
                    compradorParaEmail.getNombre(),
                    festivalParaEmail.getNombre(),
                    entradasCompradasDTOsParaEmail
            );

            return mapCompraToDTO(compraPersistidaParaEmail, entradasGeneradasPersistidas);
        } catch (Exception e) {
            handleException(e, tx, "confirmar venta con pago para PI " + paymentIntentId);
            if (e instanceof TipoEntradaNotFoundException || e instanceof FestivalNoPublicadoException || e instanceof StockInsuficienteException || e instanceof PagoInvalidoException || e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado durante la confirmación de la venta: " + e.getMessage(), e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public IniciarCompraResponseDTO iniciarProcesoPago(Integer idTipoEntrada, int cantidad)
            throws TipoEntradaNotFoundException, FestivalNoPublicadoException, IllegalArgumentException {

        log.info("Service: Iniciando proceso de pago - Entrada ID: {}, Cantidad: {}", idTipoEntrada, cantidad);
        if (idTipoEntrada == null || cantidad <= 0) {
            throw new IllegalArgumentException("ID entrada y cantidad > 0 son requeridos.");
        }

        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            TipoEntrada tipoEntrada = tipoEntradaRepository.findById(em, idTipoEntrada)
                    .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idTipoEntrada));
            validarFestivalParaCompra(tipoEntrada.getFestival());

            BigDecimal totalDecimal = tipoEntrada.getPrecio().multiply(new BigDecimal(cantidad));
            long totalCentimos = totalDecimal.multiply(new BigDecimal(100)).longValueExact();
            log.debug("Total calculado para {} entradas tipo '{}': {} {} ({} céntimos)",
                    cantidad, tipoEntrada.getTipo(), totalDecimal, EXPECTED_CURRENCY.toUpperCase(), totalCentimos);

            PaymentIntent paymentIntent = crearPaymentIntentStripe(totalCentimos);
            return new IniciarCompraResponseDTO(paymentIntent.getClientSecret());

        } catch (Exception e) {
            log.error("Error en iniciarProcesoPago para entrada ID {}: {}", idTipoEntrada, e.getMessage(), e);
            if (e instanceof TipoEntradaNotFoundException || e instanceof FestivalNoPublicadoException || e instanceof IllegalArgumentException) {
                throw e;
            }
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda ---
    private void validarParametrosConfirmacion(String email, String nombre, Integer idTipoEntrada, int cantidad, String paymentIntentId) {
        if (email == null || email.isBlank() || nombre == null || nombre.isBlank() || idTipoEntrada == null) {
            throw new IllegalArgumentException("Email, nombre, idTipoEntrada son requeridos.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser > 0.");
        }
        if (paymentIntentId == null || !paymentIntentId.startsWith("pi_")) {
            throw new IllegalArgumentException("ID PaymentIntent inválido.");
        }
    }

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

    private Compra crearYGuardarCompra(EntityManager em, Comprador comprador, BigDecimal total, PaymentIntent pi) {
        Compra compra = new Compra();
        compra.setComprador(comprador);
        compra.setTotal(total);
        compra.setStripePaymentIntentId(pi.getId());
        compra.setEstadoPago("PAGADO");
        if (pi.getCreated() != null) {
            compra.setFechaPagoConfirmado(LocalDateTime.ofInstant(Instant.ofEpochSecond(pi.getCreated()), ZoneId.systemDefault()));
        }
        return compraRepository.save(em, compra);
    }

    private CompraEntrada crearYGuardarCompraEntrada(EntityManager em, Compra compra, TipoEntrada tipoEntrada, int cantidad) {
        CompraEntrada compraEntrada = new CompraEntrada();
        compraEntrada.setCompra(compra);
        compraEntrada.setEntrada(tipoEntrada);
        compraEntrada.setCantidad(cantidad);
        compraEntrada.setPrecioUnitario(tipoEntrada.getPrecio());
        return compraEntradaRepository.save(em, compraEntrada);
    }

    private void generarYGuardarEntradasAsignadas(EntityManager em, CompraEntrada ce, int cantidad, List<Entrada> listaPersistida) {
        for (int i = 0; i < cantidad; i++) {
            Entrada ea = new Entrada();
            ea.setCompraEntrada(ce);
            ea.setEstado(EstadoEntrada.ACTIVA);
            ea.setCodigoQr(QRCodeUtil.generarContenidoQrUnico());
            listaPersistida.add(entradaRepository.save(em, ea));
        }
        log.debug("Generadas {} entradas para CompraEntrada ID: {}", cantidad, ce.getIdCompraEntrada());
    }

    private void actualizarStockEntrada(EntityManager em, TipoEntrada tipoEntrada, int cantidad) {
        int nuevoStock = tipoEntrada.getStock() - cantidad;
        tipoEntrada.setStock(nuevoStock);
        tipoEntradaRepository.save(em, tipoEntrada);
        log.info("Stock actualizado para Entrada ID {}. Nuevo stock: {}", tipoEntrada.getIdTipoEntrada(), nuevoStock);
    }

    private void validarFestivalParaCompra(Festival festival) {
        if (festival == null) {
            throw new IllegalStateException("Entrada sin festival asociado.");
        }
        if (festival.getEstado() != EstadoFestival.PUBLICADO) {
            throw new FestivalNoPublicadoException("Festival '" + festival.getNombre() + "' no está publicado.");
        }
    }

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

    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

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

    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            try {
                em.close();
            } catch (Exception e) {
                log.error("Error al cerrar EntityManager: {}", e.getMessage(), e);
            }
        }
    }

    private RuntimeException mapException(Exception e) {
        if (e instanceof AsistenteNotFoundException || e instanceof TipoEntradaNotFoundException || e instanceof FestivalNotFoundException
                || e instanceof FestivalNoPublicadoException || e instanceof StockInsuficienteException || e instanceof PagoInvalidoException
                || e instanceof IllegalArgumentException || e instanceof SecurityException || e instanceof IllegalStateException
                || e instanceof PersistenceException || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio Venta: " + e.getMessage(), e);
    }

    private CompraDTO mapCompraToDTO(Compra compra, List<Entrada> entradasGeneradas) {
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

        if (compra.getComprador() != null) {
            dto.setIdComprador(compra.getComprador().getIdComprador());
            dto.setEmailComprador(compra.getComprador().getEmail());
            dto.setNombreComprador(compra.getComprador().getNombre());
        }

        if (entradasGeneradas != null && !entradasGeneradas.isEmpty()) {
            dto.setEntradasGeneradas(entradasGeneradas.stream()
                    .map(this::mapEntradaToDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setEntradasGeneradas(new ArrayList<>());
        }

        try {
            if (compra.getDetallesCompra() != null && !compra.getDetallesCompra().isEmpty()) {
                dto.setResumenEntradas(compra.getDetallesCompra().stream()
                        .map(detalle -> detalle.getCantidad() + " x " + (detalle.getEntrada() != null ? detalle.getEntrada().getTipo() : "?"))
                        .collect(Collectors.toList()));
            } else { // Asegurar que la lista se inicialice si no hay detalles
                dto.setResumenEntradas(new ArrayList<>());
            }
        } catch (org.hibernate.LazyInitializationException e) {
            log.warn("LazyInitializationException al acceder a detallesCompra para Compra ID {}. No se incluirá resumen.", compra.getIdCompra());
            dto.setResumenEntradas(new ArrayList<>());
        }
        return dto;
    }

    private EntradaDTO mapEntradaToDTO(Entrada ea) {
        if (ea == null) {
            return null;
        }
        EntradaDTO dto = new EntradaDTO();
        dto.setIdEntrada(ea.getIdEntrada());
        dto.setCodigoQr(ea.getCodigoQr());
        dto.setEstado(ea.getEstado());
        dto.setFechaAsignacion(ea.getFechaAsignacion());
        dto.setFechaUso(ea.getFechaUso());

        if (ea.getCompraEntrada() != null) {
            dto.setIdCompraEntrada(ea.getCompraEntrada().getIdCompraEntrada());
            if (ea.getCompraEntrada().getEntrada() != null) {
                TipoEntrada tipoEntradaOriginal = ea.getCompraEntrada().getEntrada();
                dto.setIdEntradaOriginal(tipoEntradaOriginal.getIdTipoEntrada());
                dto.setTipoEntradaOriginal(tipoEntradaOriginal.getTipo());
                if (tipoEntradaOriginal.getFestival() != null) {
                    dto.setIdFestival(tipoEntradaOriginal.getFestival().getIdFestival());
                    dto.setNombreFestival(tipoEntradaOriginal.getFestival().getNombre());
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

        if (ea.getCodigoQr() != null && !ea.getCodigoQr().isBlank()) {
            String imageDataUrl = QRCodeUtil.generarQrComoBase64(ea.getCodigoQr(), 100, 100);
            if (imageDataUrl != null) {
                dto.setQrCodeImageDataUrl(imageDataUrl);
            } else {
                log.warn("No se pudo generar imagen QR para entrada {}", ea.getIdEntrada());
            }
        }
        return dto;
    }
}
