package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraResponseDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.QRCodeUtil;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de VentaService.
 */
public class VentaServiceImpl extends AbstractService implements VentaService {

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

    private record PurchaseConfirmationResult(
            CompraDTO compraDTO,
            List<EntradaDTO> entradasDTOs,
            String festivalName) {

    }

    @Override
    public CompraDTO confirmarVentaConPago(String emailComprador, String nombreComprador, String telefonoComprador, Integer idTipoEntrada, int cantidad, String paymentIntentId)
            throws TipoEntradaNotFoundException, FestivalNoPublicadoException,
            StockInsuficienteException, PagoInvalidoException, IllegalArgumentException {

        log.info("Service: Iniciando confirmación de venta - Comprador Email: {}, Entrada ID: {}, Cant: {}, PI: {}",
                emailComprador, idTipoEntrada, cantidad, paymentIntentId);

        validarParametrosConfirmacion(emailComprador, nombreComprador, idTipoEntrada, cantidad, paymentIntentId);

        Comprador compradorParaEmail = compradorService.obtenerOcrearCompradorPorEmail(emailComprador, nombreComprador, telefonoComprador);

        PaymentIntent paymentIntent = verificarPagoStripe(paymentIntentId, 0);

        PurchaseConfirmationResult result = executeTransactional(em -> {
            Comprador compradorEnTx = em.find(Comprador.class, compradorParaEmail.getIdComprador());
            if (compradorEnTx == null) {
                throw new RuntimeException("Comprador no encontrado en el contexto transaccional.");
            }

            TipoEntrada tipoEntradaEnTx = tipoEntradaRepository.findById(em, idTipoEntrada, LockModeType.PESSIMISTIC_WRITE)
                    .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idTipoEntrada));

            Festival festivalEnTx = tipoEntradaEnTx.getFestival();
            if (festivalEnTx == null) {
                throw new IllegalStateException("Tipo de entrada ID " + idTipoEntrada + " sin festival asociado.");
            }
            if (festivalEnTx.getEstado() != EstadoFestival.PUBLICADO) {
                throw new FestivalNoPublicadoException("Festival '" + festivalEnTx.getNombre() + "' no está publicado.");
            }

            BigDecimal totalEsperadoDecimalTx = tipoEntradaEnTx.getPrecio().multiply(new BigDecimal(cantidad));
            long totalEsperadoCentimosTx = totalEsperadoDecimalTx.multiply(new BigDecimal(100)).longValueExact();

            if (paymentIntent.getAmount() == null || paymentIntent.getAmount() != totalEsperadoCentimosTx) {
                throw new PagoInvalidoException("Monto del pago Stripe (" + paymentIntent.getAmount() + ") no coincide con el esperado (" + totalEsperadoCentimosTx + ").");
            }

            if (tipoEntradaEnTx.getStock() == null || tipoEntradaEnTx.getStock() < cantidad) {
                throw new StockInsuficienteException("Stock (" + tipoEntradaEnTx.getStock() + ") insuficiente para entrada '" + tipoEntradaEnTx.getTipo() + "'.");
            }

            Compra compra = crearYGuardarCompra(em, compradorEnTx, totalEsperadoDecimalTx, paymentIntent);
            CompraEntrada compraEntrada = crearYGuardarCompraEntrada(em, compra, tipoEntradaEnTx, cantidad);

            List<Entrada> entradasGeneradasPersistidas = new ArrayList<>();
            generarYGuardarEntradasAsignadas(em, compraEntrada, cantidad, entradasGeneradasPersistidas);
            actualizarStockEntrada(em, tipoEntradaEnTx, cantidad);

            List<EntradaDTO> entradasCompradasDTOs = entradasGeneradasPersistidas.stream()
                    .map(this::mapEntradaToDTO)
                    .collect(Collectors.toList());

            log.info("Venta confirmada and TX completed. Compra ID: {}, PI: {}", compra.getIdCompra(), paymentIntentId);

            return new PurchaseConfirmationResult(
                    mapCompraToDTO(compra, entradasGeneradasPersistidas),
                    entradasCompradasDTOs,
                    festivalEnTx.getNombre()
            );
        }, "confirmarVentaConPago " + paymentIntentId);

        emailService.enviarEmailEntradasCompradas(
                compradorParaEmail.getEmail(),
                compradorParaEmail.getNombre(),
                result.festivalName(),
                result.entradasDTOs()
        );

        return result.compraDTO();
    }

    @Override
    public IniciarCompraResponseDTO iniciarProcesoPago(Integer idTipoEntrada, int cantidad)
            throws TipoEntradaNotFoundException, FestivalNoPublicadoException, IllegalArgumentException {

        log.info("Service: Iniciando proceso de pago - Entrada ID: {}, Cantidad: {}", idTipoEntrada, cantidad);
        if (idTipoEntrada == null || cantidad <= 0) {
            throw new IllegalArgumentException("ID entrada y cantidad > 0 son requeridos.");
        }

        return executeRead(em -> {
            TipoEntrada tipoEntrada = tipoEntradaRepository.findById(em, idTipoEntrada)
                    .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idTipoEntrada));
            validarFestivalParaCompra(tipoEntrada.getFestival());

            BigDecimal totalDecimal = tipoEntrada.getPrecio().multiply(new BigDecimal(cantidad));
            long totalCentimos = totalDecimal.multiply(new BigDecimal(100)).longValueExact();
            log.debug("Total calculado para {} entradas tipo '{}': {} {} ({} céntimos)",
                    cantidad, tipoEntrada.getTipo(), totalDecimal, EXPECTED_CURRENCY.toUpperCase(), totalCentimos);

            PaymentIntent paymentIntent = crearPaymentIntentStripe(totalCentimos);
            return new IniciarCompraResponseDTO(paymentIntent.getClientSecret());
        }, "iniciarProcesoPago " + idTipoEntrada);
    }

    // --- Métodos privados de ayuda ---
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
        compraEntrada.setTipoEntrada(tipoEntrada);
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

        if (compra.getDetallesCompra() != null && !compra.getDetallesCompra().isEmpty()) {
            dto.setResumenEntradas(compra.getDetallesCompra().stream()
                    .map(detalle -> detalle.getCantidad() + " x " + (detalle.getTipoEntrada() != null ? detalle.getTipoEntrada().getTipo() : "?"))
                    .collect(Collectors.toList()));
        } else {
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
        if (ea.getFechaAsignacion() != null) {
            dto.setFechaAsignacion(Date.from(ea.getFechaAsignacion().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (ea.getFechaUso() != null) {
            dto.setFechaUso(Date.from(ea.getFechaUso().atZone(ZoneId.systemDefault()).toInstant()));
        }

        if (ea.getCompraEntrada() != null) {
            dto.setIdCompraEntrada(ea.getCompraEntrada().getIdCompraEntrada());
            if (ea.getCompraEntrada().getTipoEntrada() != null) {
                TipoEntrada tipoEntradaOriginal = ea.getCompraEntrada().getTipoEntrada();
                dto.setIdEntradaOriginal(tipoEntradaOriginal.getIdTipoEntrada());
                dto.setTipoEntradaOriginal(tipoEntradaOriginal.getTipo());
                dto.setRequiereNominacion(tipoEntradaOriginal.getRequiereNominacion());

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
