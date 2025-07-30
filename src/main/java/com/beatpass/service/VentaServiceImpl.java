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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
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
 * Implementación del servicio para la gestión del proceso de venta,
 * refactorizada para usar CDI y JTA.
 */
@ApplicationScoped
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    @Inject
    private CompradorService compradorService;
    @Inject
    private TipoEntradaRepository tipoEntradaRepository;
    @Inject
    private CompraRepository compraRepository;
    @Inject
    private CompraEntradaRepository compraEntradaRepository;
    @Inject
    private EntradaRepository entradaRepository;
    @Inject
    private EmailService emailService;
    @Inject
    private CompraMapper compraMapper;
    @Inject
    private EntradaMapper entradaMapper;

    @PersistenceContext(unitName = "beatpassPersistenceUnit")
    private EntityManager em;

    private static final String EXPECTED_CURRENCY = "eur";

    /**
     * Registro interno para encapsular los resultados de la transacción de
     * compra y pasarlos de forma segura al paso de envío de correo, que se
     * ejecuta fuera de la transacción.
     *
     * @param compraDTO DTO de la compra confirmada.
     * @param entradasDTOs Lista de DTOs de las entradas generadas.
     * @param festivalName Nombre del festival para el correo.
     */
    private record PurchaseConfirmationResult(CompraDTO compraDTO, List<EntradaDTO> entradasDTOs, String festivalName) {

    }

    @Override
    @Transactional(rollbackOn = {Exception.class})
    public CompraDTO confirmarVentaConPago(String emailComprador, String nombreComprador, String telefonoComprador, Integer idTipoEntrada, int cantidad, String paymentIntentId)
            throws TipoEntradaNotFoundException, FestivalNoPublicadoException, StockInsuficienteException, PagoInvalidoException, IllegalArgumentException {

        log.info("Service: Iniciando confirmación de venta - Comprador Email: {}, Entrada ID: {}, Cant: {}, PI: {}",
                emailComprador, idTipoEntrada, cantidad, paymentIntentId);

        validarParametrosConfirmacion(emailComprador, nombreComprador, idTipoEntrada, cantidad, paymentIntentId);

        Comprador compradorParaEmail = compradorService.obtenerOcrearCompradorPorEmail(emailComprador, nombreComprador, telefonoComprador);

        PaymentIntent paymentIntent = verificarPagoStripe(paymentIntentId);

        TipoEntrada tipoEntradaEnTx = tipoEntradaRepository.findById(idTipoEntrada)
                .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idTipoEntrada));
        em.lock(tipoEntradaEnTx, LockModeType.PESSIMISTIC_WRITE);

        validarFestivalParaCompra(tipoEntradaEnTx.getFestival());

        BigDecimal totalEsperadoDecimalTx = tipoEntradaEnTx.getPrecio().multiply(new BigDecimal(cantidad));
        long totalEsperadoCentimosTx = totalEsperadoDecimalTx.multiply(new BigDecimal(100)).longValueExact();

        if (paymentIntent.getAmount() == null || paymentIntent.getAmount() != totalEsperadoCentimosTx) {
            throw new PagoInvalidoException("Monto del pago Stripe (" + paymentIntent.getAmount() + ") no coincide con el esperado (" + totalEsperadoCentimosTx + ").");
        }

        if (tipoEntradaEnTx.getStock() == null || tipoEntradaEnTx.getStock() < cantidad) {
            throw new StockInsuficienteException("Stock (" + tipoEntradaEnTx.getStock() + ") insuficiente para entrada '" + tipoEntradaEnTx.getTipo() + "'.");
        }

        Compra compra = crearYGuardarCompra(compradorParaEmail, totalEsperadoDecimalTx, paymentIntent);
        CompraEntrada compraEntrada = crearYGuardarCompraEntrada(compra, tipoEntradaEnTx, cantidad);

        List<Entrada> entradasGeneradasPersistidas = generarYGuardarEntradasAsignadas(compraEntrada, cantidad);
        actualizarStockEntrada(tipoEntradaEnTx, cantidad);

        List<EntradaDTO> entradasCompradasDTOs = entradasGeneradasPersistidas.stream()
                .map(entradaMapper::entradaToEntradaDTO)
                .collect(Collectors.toList());

        CompraDTO finalCompraDTO = compraMapper.compraToCompraDTO(compra);
        finalCompraDTO.setEntradasGeneradas(entradasCompradasDTOs);

        log.info("Venta confirmada y TX completada. Compra ID: {}, PI: {}", compra.getIdCompra(), paymentIntentId);

        PurchaseConfirmationResult result = new PurchaseConfirmationResult(
                finalCompraDTO,
                entradasCompradasDTOs,
                tipoEntradaEnTx.getFestival().getNombre()
        );

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

        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(idTipoEntrada)
                .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idTipoEntrada));

        validarFestivalParaCompra(tipoEntrada.getFestival());

        BigDecimal totalDecimal = tipoEntrada.getPrecio().multiply(new BigDecimal(cantidad));
        long totalCentimos = totalDecimal.multiply(new BigDecimal(100)).longValueExact();
        log.debug("Total calculado para {} entradas tipo '{}': {} {} ({} céntimos)",
                cantidad, tipoEntrada.getTipo(), totalDecimal, EXPECTED_CURRENCY.toUpperCase(), totalCentimos);

        PaymentIntent paymentIntent = crearPaymentIntentStripe(totalCentimos);
        return new IniciarCompraResponseDTO(paymentIntent.getClientSecret());
    }

    // --- MÉTODOS PRIVADOS ---
    /**
     * Valida los parámetros de entrada para la confirmación de una venta.
     *
     * @param email Email del comprador.
     * @param nombre Nombre del comprador.
     * @param idTipoEntrada ID del tipo de entrada.
     * @param cantidad Cantidad de entradas.
     * @param paymentIntentId ID del Payment Intent de Stripe.
     * @throws IllegalArgumentException si algún parámetro es nulo, vacío o
     * inválido.
     */
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

    /**
     * Verifica el estado de un PaymentIntent de Stripe para asegurar que el
     * pago fue exitoso.
     *
     * @param paymentIntentId El ID del PaymentIntent a verificar.
     * @return El objeto PaymentIntent si el pago fue exitoso.
     * @throws PagoInvalidoException si el pago no está en estado 'succeeded' o
     * si hay un error al comunicarse con Stripe.
     */
    private PaymentIntent verificarPagoStripe(String paymentIntentId) throws PagoInvalidoException {
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

    /**
     * Crea y persiste una nueva entidad Compra.
     *
     * @param comprador El comprador asociado.
     * @param total El monto total de la compra.
     * @param pi El PaymentIntent de Stripe verificado.
     * @return La entidad Compra persistida.
     */
    private Compra crearYGuardarCompra(Comprador comprador, BigDecimal total, PaymentIntent pi) {
        Compra compra = new Compra();
        compra.setComprador(comprador);
        compra.setTotal(total);
        compra.setStripePaymentIntentId(pi.getId());
        compra.setEstadoPago("PAGADO");
        if (pi.getCreated() != null) {
            compra.setFechaPagoConfirmado(LocalDateTime.ofInstant(Instant.ofEpochSecond(pi.getCreated()), ZoneId.systemDefault()));
        }
        return compraRepository.save(compra);
    }

    /**
     * Crea y persiste una nueva entidad CompraEntrada (línea de detalle de la
     * compra).
     *
     * @param compra La compra a la que pertenece el detalle.
     * @param tipoEntrada El tipo de entrada comprado.
     * @param cantidad La cantidad de entradas de este tipo.
     * @return La entidad CompraEntrada persistida.
     */
    private CompraEntrada crearYGuardarCompraEntrada(Compra compra, TipoEntrada tipoEntrada, int cantidad) {
        CompraEntrada compraEntrada = new CompraEntrada();
        compraEntrada.setCompra(compra);
        compraEntrada.setTipoEntrada(tipoEntrada);
        compraEntrada.setCantidad(cantidad);
        compraEntrada.setPrecioUnitario(tipoEntrada.getPrecio());
        compraEntradaRepository.save(compraEntrada);
        return compraEntrada;
    }

    /**
     * Genera y persiste las entradas individuales para un detalle de compra.
     *
     * @param ce El detalle de compra del que se generan las entradas.
     * @param cantidad La cantidad de entradas a generar.
     * @return Una lista con las entidades Entrada persistidas.
     */
    private List<Entrada> generarYGuardarEntradasAsignadas(CompraEntrada ce, int cantidad) {
        List<Entrada> listaPersistida = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            Entrada ea = new Entrada();
            ea.setCompraEntrada(ce);
            ea.setEstado(EstadoEntrada.ACTIVA);
            ea.setCodigoQr(QRCodeUtil.generarContenidoQrUnico());
            listaPersistida.add(entradaRepository.save(ea));
        }
        log.debug("Generadas {} entradas para CompraEntrada ID: {}", cantidad, ce.getIdCompraEntrada());
        return listaPersistida;
    }

    /**
     * Actualiza el stock de un tipo de entrada después de una venta.
     *
     * @param tipoEntrada El tipo de entrada cuyo stock se va a reducir.
     * @param cantidad La cantidad vendida.
     */
    private void actualizarStockEntrada(TipoEntrada tipoEntrada, int cantidad) {
        int nuevoStock = tipoEntrada.getStock() - cantidad;
        tipoEntrada.setStock(nuevoStock);
        tipoEntradaRepository.save(tipoEntrada);
        log.info("Stock actualizado para Entrada ID {}. Nuevo stock: {}", tipoEntrada.getIdTipoEntrada(), nuevoStock);
    }

    /**
     * Valida si un festival está en un estado válido para permitir la compra de
     * entradas.
     *
     * @param festival El festival a validar.
     * @throws FestivalNoPublicadoException si el festival no está en estado
     * PUBLICADO.
     * @throws IllegalStateException si el festival es nulo.
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
     * Crea un PaymentIntent en Stripe para iniciar un proceso de pago.
     *
     * @param totalCentimos El monto total a cobrar en céntimos.
     * @return El objeto PaymentIntent creado por Stripe.
     * @throws RuntimeException si hay un error al comunicarse con la API de
     * Stripe.
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
}
