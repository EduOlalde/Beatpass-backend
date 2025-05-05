package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
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

    // Constante para la moneda esperada (ajustar si es necesario, ej: "usd")
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
        Entrada entrada; // Declarar fuera para usar en cálculo de total

        try {
            // --- Paso 1: Validaciones previas y cálculo del total esperado (sin TX aún) ---
            em = JPAUtil.createEntityManager(); // Crear EM para lecturas iniciales

            // Buscar Asistente
            Asistente asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));

            // Buscar Tipo de Entrada (sin bloqueo aún)
            entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            // Verificar Festival y su Estado
            Festival festival = entrada.getFestival();
            if (festival == null) {
                log.error("Inconsistencia: Entrada ID {} sin festival asociado.", idEntrada);
                throw new IllegalStateException("Error interno: La entrada no está asociada a ningún festival.");
            }
            if (festival.getEstado() != EstadoFestival.PUBLICADO) {
                log.warn("Intento de compra para festival ID {} no PUBLICADO (Estado: {})", festival.getIdFestival(), festival.getEstado());
                throw new FestivalNoPublicadoException("No se pueden comprar entradas para el festival '"
                        + festival.getNombre() + "' (estado: " + festival.getEstado() + ").");
            }

            // Calcular el total esperado (en la menor unidad de la moneda, ej. céntimos para EUR)
            BigDecimal totalEsperadoDecimal = entrada.getPrecio().multiply(new BigDecimal(cantidad));
            long totalEsperadoCentimos = totalEsperadoDecimal.multiply(new BigDecimal(100)).longValueExact();

            closeEntityManager(em); // Cerrar EM usado para lecturas iniciales

            // --- Paso 2: Verificación del Pago con Stripe ---
            log.debug("Verificando PaymentIntent de Stripe: {}", paymentIntentId);
            PaymentIntent paymentIntent;
            try {
                PaymentIntentRetrieveParams params = PaymentIntentRetrieveParams.builder().build();
                paymentIntent = PaymentIntent.retrieve(paymentIntentId, params, null);

                if (!"succeeded".equals(paymentIntent.getStatus())) {
                    log.warn("Stripe PaymentIntent {} no está en estado 'succeeded'. Estado actual: {}", paymentIntentId, paymentIntent.getStatus());
                    throw new PagoInvalidoException("El pago no se ha completado correctamente (Estado: " + paymentIntent.getStatus() + ")");
                }
                if (paymentIntent.getAmount() == null || paymentIntent.getAmount() != totalEsperadoCentimos) {
                    log.warn("Discrepancia en el monto del pago. Esperado: {} céntimos, Recibido: {} céntimos", totalEsperadoCentimos, paymentIntent.getAmount());
                    throw new PagoInvalidoException("El monto del pago (" + paymentIntent.getAmount() + ") no coincide con el total esperado (" + totalEsperadoCentimos + ").");
                }
                if (!EXPECTED_CURRENCY.equalsIgnoreCase(paymentIntent.getCurrency())) {
                    log.warn("Discrepancia en la moneda del pago. Esperada: {}, Recibida: {}", EXPECTED_CURRENCY, paymentIntent.getCurrency());
                    throw new PagoInvalidoException("La moneda del pago (" + paymentIntent.getCurrency() + ") no coincide con la esperada (" + EXPECTED_CURRENCY + ").");
                }
                log.info("Verificación de Stripe PaymentIntent {} exitosa (succeeded, monto y moneda OK).", paymentIntentId);

            } catch (StripeException e) {
                log.error("Error al verificar PaymentIntent {} con Stripe: {}", paymentIntentId, e.getMessage());
                throw new PagoInvalidoException("Error al verificar el estado del pago con la pasarela: " + e.getMessage(), e);
            }

            // --- Paso 3: Proceder con la lógica de negocio (Ahora dentro de una transacción) ---
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin(); // Iniciar transacción

            // Volver a buscar Asistente (dentro de la TX)
            asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));

            // Volver a buscar Entrada y BLOQUEARLA ahora
            entrada = em.find(Entrada.class, idEntrada, LockModeType.PESSIMISTIC_WRITE);
            if (entrada == null) {
                throw new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada);
            }
            log.debug("Entrada ID {} encontrada y bloqueada dentro de TX. Stock actual: {}", idEntrada, entrada.getStock());

            // Re-verificar stock (importante por el bloqueo)
            if (entrada.getStock() == null || entrada.getStock() < cantidad) {
                log.warn("Stock insuficiente (verificado dentro de TX con bloqueo) para entrada ID {}. Solicitado: {}, Disponible: {}",
                        idEntrada, cantidad, entrada.getStock());
                throw new StockInsuficienteException("Lo sentimos, el stock (" + entrada.getStock() + ") para el tipo de entrada '"
                        + entrada.getTipo() + "' se agotó mientras procesabas el pago.");
            }

            // Crear la Compra con datos de Stripe
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
            log.debug("Compra ID: {} creada con datos de Stripe para Asistente ID: {}", compra.getIdCompra(), idAsistente);

            // Crear CompraEntrada
            CompraEntrada compraEntrada = new CompraEntrada();
            compraEntrada.setCompra(compra);
            compraEntrada.setEntrada(entrada);
            compraEntrada.setCantidad(cantidad);
            compraEntrada.setPrecioUnitario(entrada.getPrecio());
            compraEntrada = compraEntradaRepository.save(em, compraEntrada);
            log.debug("CompraEntrada ID: {} creada para Compra ID: {}", compraEntrada.getIdCompraEntrada(), compra.getIdCompra());

            // Generar Entradas Asignadas
            List<EntradaAsignada> entradasGeneradas = new ArrayList<>();
            for (int i = 0; i < cantidad; i++) {
                EntradaAsignada entradaAsignada = new EntradaAsignada();
                entradaAsignada.setCompraEntrada(compraEntrada);
                entradaAsignada.setEstado(EstadoEntradaAsignada.ACTIVA);
                String qrContent = QRCodeUtil.generarContenidoQrUnico();
                entradaAsignada.setCodigoQr(qrContent);
                entradaAsignada = entradaAsignadaRepository.save(em, entradaAsignada);
                entradasGeneradas.add(entradaAsignada);
                log.trace("Generada y guardada EntradaAsignada ID: {} con QR final: ...{}",
                        entradaAsignada.getIdEntradaAsignada(),
                        qrContent.substring(Math.max(0, qrContent.length() - 6)));
            }
            log.debug("Generadas {} entradas asignadas para CompraEntrada ID: {}", cantidad, compraEntrada.getIdCompraEntrada());

            // Actualizar Stock
            int nuevoStock = entrada.getStock() - cantidad;
            entrada.setStock(nuevoStock);
            entradaRepository.save(em, entrada);
            log.info("Stock actualizado para Entrada ID {}. Nuevo stock: {}", idEntrada, nuevoStock);

            // Confirmar transacción
            tx.commit();
            log.info("Venta confirmada con pago exitoso y transacción completada. Compra ID: {}, PI: {}",
                    compra.getIdCompra(), paymentIntentId);

            // Mapear Compra a CompraDTO para devolver
            CompraDTO compraDTO = mapCompraToDTO(compra);
            return compraDTO;

        } catch (Exception e) {
            handleException(e, tx, "confirmar venta con pago para PI " + paymentIntentId);
            throw mapException(e); // Relanzar excepción mapeada
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Este método calcula el total, crea un PaymentIntent en Stripe y devuelve
     * el client_secret necesario para que el frontend procese el pago. No
     * realiza operaciones de base de datos persistentes (solo lectura).
     *
     * @param idEntrada ID del tipo de entrada deseado. No debe ser
     * {@code null}.
     * @param cantidad Número de entradas deseadas. Debe ser > 0.
     * @return Un {@link IniciarCompraResponseDTO} que contiene el
     * client_secret.
     * @throws EntradaNotFoundException Si el tipo de entrada no existe.
     * @throws FestivalNoPublicadoException Si el festival asociado no está
     * publicado.
     * @throws IllegalArgumentException Si idEntrada es null o cantidad <= 0.
     * @throws RuntimeException Si ocurre un error al calcular el total o al
     * interactuar con Stripe.
     */
    @Override
    public IniciarCompraResponseDTO iniciarProcesoPago(Integer idEntrada, int cantidad)
            throws EntradaNotFoundException, FestivalNoPublicadoException, IllegalArgumentException {

        log.info("Service: Iniciando proceso de pago - Entrada ID: {}, Cantidad: {}", idEntrada, cantidad);

        // Validación inicial
        if (idEntrada == null) {
            throw new IllegalArgumentException("ID de entrada es requerido.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de entradas debe ser mayor que cero.");
        }

        EntityManager em = null;
        try {
            // --- Obtener datos necesarios (solo lectura) ---
            em = JPAUtil.createEntityManager();

            // Buscar Tipo de Entrada
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            // Verificar Festival y Estado
            Festival festival = entrada.getFestival();
            if (festival == null) {
                log.error("Inconsistencia: Entrada ID {} sin festival asociado.", idEntrada);
                throw new IllegalStateException("Error interno: La entrada no está asociada a ningún festival.");
            }
            if (festival.getEstado() != EstadoFestival.PUBLICADO) {
                log.warn("Intento de iniciar pago para festival ID {} no PUBLICADO (Estado: {})", festival.getIdFestival(), festival.getEstado());
                throw new FestivalNoPublicadoException("No se pueden comprar entradas para el festival '"
                        + festival.getNombre() + "' (estado: " + festival.getEstado() + ").");
            }

            // Calcular total en céntimos
            BigDecimal totalDecimal = entrada.getPrecio().multiply(new BigDecimal(cantidad));
            long totalCentimos = totalDecimal.multiply(new BigDecimal(100)).longValueExact();
            log.debug("Total calculado para {} entradas de tipo '{}': {} {} ({} céntimos)",
                    cantidad, entrada.getTipo(), totalDecimal, EXPECTED_CURRENCY.toUpperCase(), totalCentimos);

            // --- Crear PaymentIntent en Stripe ---
            log.debug("Creando PaymentIntent en Stripe...");
            PaymentIntent paymentIntent;
            try {
                // Parámetros para crear el PaymentIntent
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(totalCentimos) // Monto en la menor unidad (céntimos)
                        .setCurrency(EXPECTED_CURRENCY) // Moneda (ej: "eur")
                        // Habilitar métodos de pago automáticos (recomendado por Stripe)
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        // Puedes añadir metadata si es útil (ej: idEntrada, cantidad)
                        // .putMetadata("idEntrada", idEntrada.toString())
                        // .putMetadata("cantidad", String.valueOf(cantidad))
                        // .putMetadata("nombreFestival", festival.getNombre()) // Cuidado con PII
                        .build();

                // Crear el PaymentIntent
                paymentIntent = PaymentIntent.create(params);
                log.info("PaymentIntent de Stripe creado con ID: {}", paymentIntent.getId());

            } catch (StripeException e) {
                log.error("Error al crear PaymentIntent en Stripe para entrada ID {}: {}", idEntrada, e.getMessage(), e);
                // Mapear a una excepción genérica o una más específica si se prefiere
                throw new RuntimeException("Error al iniciar el proceso de pago con la pasarela: " + e.getMessage(), e);
            }

            // Devolver el client_secret
            return new IniciarCompraResponseDTO(paymentIntent.getClientSecret());

        } catch (Exception e) {
            // Capturar excepciones de BD o de cálculo de total
            log.error("Error en iniciarProcesoPago para entrada ID {}: {}", idEntrada, e.getMessage(), e);
            // Relanzar excepción mapeada (podría ser EntradaNotFound, FestivalNoPublicado, etc.)
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda (Helpers) ---
    /**
     * Manejador genérico de excepciones para métodos de servicio.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("Rollback de transacción de '{}' realizado debido a error.", action);
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
        if (e instanceof AsistenteNotFoundException
                || e instanceof EntradaNotFoundException
                || e instanceof FestivalNotFoundException
                || e instanceof FestivalNoPublicadoException
                || e instanceof StockInsuficienteException
                || e instanceof PagoInvalidoException
                || e instanceof IllegalArgumentException
                || e instanceof SecurityException
                || e instanceof IllegalStateException
                || e instanceof PersistenceException
                || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        log.error("Encontrada excepción chequeada inesperada, envolviendo en RuntimeException: {}", e.getClass().getName());
        return new RuntimeException("Error inesperado en la capa de servicio Venta: " + e.getMessage(), e);
    }

    /**
     * Mapea una entidad {@link Compra} a su correspondiente {@link CompraDTO}.
     *
     * @param compra La entidad {@link Compra} a mapear. No debe ser
     * {@code null}.
     * @return El objeto {@link CompraDTO} poblado.
     */
    private CompraDTO mapCompraToDTO(Compra compra) {
        if (compra == null) {
            log.warn("Intento de mapear una entidad Compra nula a DTO.");
            return null;
        }
        CompraDTO dto = new CompraDTO();
        dto.setIdCompra(compra.getIdCompra());
        dto.setFechaCompra(compra.getFechaCompra());
        dto.setTotal(compra.getTotal());

        // Mapear información del pago (si existe)
        dto.setStripePaymentIntentId(compra.getStripePaymentIntentId());
        dto.setEstadoPago(compra.getEstadoPago());
        dto.setFechaPagoConfirmado(compra.getFechaPagoConfirmado());

        // Mapear información del asistente asociado
        if (compra.getAsistente() != null) {
            dto.setIdAsistente(compra.getAsistente().getIdAsistente());
            dto.setEmailAsistente(compra.getAsistente().getEmail());
            dto.setNombreAsistente(compra.getAsistente().getNombre());
        } else {
            log.warn("La Compra ID {} no tiene un Asistente asociado.", compra.getIdCompra());
        }

        // Mapear los detalles de la compra a un resumen simple
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
        }
        return dto;
    }

    /**
     * Excepción personalizada para indicar problemas durante la verificación o
     * procesamiento del pago con la pasarela externa (Stripe).
     */
    public static class PagoInvalidoException extends RuntimeException {

        public PagoInvalidoException(String message) {
            super(message);
        }

        public PagoInvalidoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
