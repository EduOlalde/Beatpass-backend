package com.beatpass.service;

import com.beatpass.dto.CompraDTO;
import com.beatpass.dto.EntradaDTO;
import com.beatpass.dto.IniciarCompraResponseDTO;
import com.beatpass.exception.FestivalNoPublicadoException;
import com.beatpass.exception.PagoInvalidoException;
import com.beatpass.exception.StockInsuficienteException;
import com.beatpass.exception.TipoEntradaNotFoundException;
import com.beatpass.mapper.CompraMapper;
import com.beatpass.mapper.EntradaMapper;
import com.beatpass.model.*;
import com.beatpass.repository.*;
import com.beatpass.util.QRCodeUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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
 * Implementación del servicio para la gestión del proceso de venta.
 */
public class VentaServiceImpl extends AbstractService implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    private final CompradorService compradorService;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final CompraRepository compraRepository;
    private final CompraEntradaRepository compraEntradaRepository;
    private final EntradaRepository entradaRepository;
    private final EmailService emailService;
    private final CompraMapper compraMapper;
    private final EntradaMapper entradaMapper;

    private static final String EXPECTED_CURRENCY = "eur";

    @Inject
    public VentaServiceImpl(CompradorService compradorService, TipoEntradaRepository tipoEntradaRepository, CompraRepository compraRepository, CompraEntradaRepository compraEntradaRepository, EntradaRepository entradaRepository, EmailService emailService) {
        this.compradorService = compradorService;
        this.tipoEntradaRepository = tipoEntradaRepository;
        this.compraRepository = compraRepository;
        this.compraEntradaRepository = compraEntradaRepository;
        this.entradaRepository = entradaRepository;
        this.emailService = emailService;
        this.compraMapper = CompraMapper.INSTANCE;
        this.entradaMapper = EntradaMapper.INSTANCE;
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
                    .map(entradaMapper::entradaToEntradaDTO)
                    .collect(Collectors.toList());

            CompraDTO finalCompraDTO = compraMapper.compraToCompraDTO(compra);
            finalCompraDTO.setEntradasGeneradas(entradasCompradasDTOs);

            log.info("Venta confirmada and TX completed. Compra ID: {}, PI: {}", compra.getIdCompra(), paymentIntentId);

            return new PurchaseConfirmationResult(
                    finalCompraDTO,
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
}
