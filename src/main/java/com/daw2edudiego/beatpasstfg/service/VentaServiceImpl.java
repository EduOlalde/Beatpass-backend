package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import com.daw2edudiego.beatpasstfg.util.QRCodeUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la interfaz {@link VentaService}. Orquesta el proceso
 * completo de registro de una venta de entradas, asegurando la atomicidad de la
 * operación a través de transacciones JPA y manejando la lógica de negocio
 * relacionada con la validación de datos, disponibilidad de stock, generación
 * de entradas individuales y actualización del inventario.
 *
 * @see VentaService
 * @see CompraRepository
 * @see EntradaRepository
 * @see EntradaAsignadaRepository
 * @author Eduardo Olalde
 */
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    // Inyección manual de dependencias (en un entorno con CDI/Spring se usaría @Inject/@Autowired)
    private final AsistenteRepository asistenteRepository;
    private final EntradaRepository entradaRepository;
    private final CompraRepository compraRepository;
    private final CompraEntradaRepository compraEntradaRepository;
    private final EntradaAsignadaRepository entradaAsignadaRepository;
    // No necesitamos FestivalRepository directamente si accedemos al festival via Entrada

    /**
     * Constructor que inicializa los repositorios necesarios.
     */
    public VentaServiceImpl() {
        this.asistenteRepository = new AsistenteRepositoryImpl();
        this.entradaRepository = new EntradaRepositoryImpl();
        this.compraRepository = new CompraRepositoryImpl();
        this.compraEntradaRepository = new CompraEntradaRepositoryImpl();
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        // this.festivalRepository = new FestivalRepositoryImpl(); // No es estrictamente necesario aquí
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registrarVenta(Integer idAsistente, Integer idEntrada, int cantidad) {
        log.info("Service: Iniciando registro de venta - Asistente ID: {}, Entrada ID: {}, Cantidad: {}",
                idAsistente, idEntrada, cantidad);

        // Validación inicial de parámetros
        if (idAsistente == null || idEntrada == null) {
            throw new IllegalArgumentException("ID de asistente y ID de entrada son requeridos.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de entradas debe ser mayor que cero.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin(); // Iniciar transacción

            // 1. Buscar Asistente
            Asistente asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));

            // 2. Buscar Tipo de Entrada y bloquearla para actualizar stock
            // Usamos PESSIMISTIC_WRITE para asegurar que la lectura del stock y su posterior
            // decremento sean atómicos respecto a otras transacciones concurrentes.
            Entrada entrada = em.find(Entrada.class, idEntrada, LockModeType.PESSIMISTIC_WRITE);
            if (entrada == null) {
                throw new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada);
            }
            log.debug("Entrada ID {} encontrada y bloqueada. Tipo: {}, Stock actual: {}",
                    entrada.getIdEntrada(), entrada.getTipo(), entrada.getStock());

            // 3. Verificar Festival y su Estado
            Festival festival = entrada.getFestival();
            if (festival == null) {
                log.error("Inconsistencia de datos: La entrada ID {} no tiene un festival asociado.", idEntrada);
                throw new IllegalStateException("Error interno: La entrada no está asociada a ningún festival.");
            }
            if (festival.getEstado() != EstadoFestival.PUBLICADO) {
                log.warn("Intento de compra para festival ID {} que no está PUBLICADO (Estado: {})",
                        festival.getIdFestival(), festival.getEstado());
                throw new FestivalNoPublicadoException("No se pueden comprar entradas para el festival '"
                        + festival.getNombre() + "' (estado: " + festival.getEstado() + ").");
            }
            log.debug("Festival asociado ID {} verificado como PUBLICADO.", festival.getIdFestival());

            // 4. Validar Stock disponible
            if (entrada.getStock() == null || entrada.getStock() < cantidad) { // Verificar también nulidad del stock
                log.warn("Stock insuficiente para entrada ID {}. Solicitado: {}, Disponible: {}",
                        idEntrada, cantidad, entrada.getStock());
                throw new StockInsuficienteException("No hay suficiente stock (" + entrada.getStock() + ") para el tipo de entrada '"
                        + entrada.getTipo() + "'.");
            }

            // 5. Crear la cabecera de la Compra
            Compra compra = new Compra();
            compra.setAsistente(asistente);
            BigDecimal totalCompra = entrada.getPrecio().multiply(new BigDecimal(cantidad));
            compra.setTotal(totalCompra);
            compra = compraRepository.save(em, compra); // Guardar para obtener ID
            log.debug("Compra ID: {} creada para Asistente ID: {}", compra.getIdCompra(), idAsistente);

            // 6. Crear el detalle de CompraEntrada
            CompraEntrada compraEntrada = new CompraEntrada();
            compraEntrada.setCompra(compra);
            compraEntrada.setEntrada(entrada);
            compraEntrada.setCantidad(cantidad);
            compraEntrada.setPrecioUnitario(entrada.getPrecio()); // Precio en el momento de la compra
            compraEntrada = compraEntradaRepository.save(em, compraEntrada); // Guardar para obtener ID
            log.debug("CompraEntrada ID: {} creada para Compra ID: {}", compraEntrada.getIdCompraEntrada(), compra.getIdCompra());

            // 7. Generar las Entradas Asignadas individuales
            List<EntradaAsignada> entradasGeneradas = new ArrayList<>();
            for (int i = 0; i < cantidad; i++) {
                EntradaAsignada entradaAsignada = new EntradaAsignada();
                entradaAsignada.setCompraEntrada(compraEntrada);
                entradaAsignada.setEstado(EstadoEntradaAsignada.ACTIVA);
                // Generar código QR único
                String qrContent = QRCodeUtil.generarContenidoQrUnico();
                entradaAsignada.setCodigoQr(qrContent);

                // Guardar cada entrada asignada
                entradaAsignada = entradaAsignadaRepository.save(em, entradaAsignada);
                entradasGeneradas.add(entradaAsignada);
                log.trace("Generada y guardada EntradaAsignada ID: {} con QR: ...{}",
                        entradaAsignada.getIdEntradaAsignada(),
                        qrContent.substring(Math.max(0, qrContent.length() - 6))); // Loggear solo el final del QR
            }
            log.debug("Generadas {} entradas asignadas para CompraEntrada ID: {}", cantidad, compraEntrada.getIdCompraEntrada());

            // 8. Actualizar el Stock de la Entrada original
            int nuevoStock = entrada.getStock() - cantidad;
            entrada.setStock(nuevoStock);
            entradaRepository.save(em, entrada); // Guardar la entidad Entrada actualizada
            log.info("Stock actualizado para Entrada ID {}. Nuevo stock: {}", idEntrada, nuevoStock);

            // 9. Confirmar la transacción
            tx.commit();
            log.info("Venta registrada exitosamente. Compra ID: {}, Asistente ID: {}, Entrada ID: {}, Cantidad: {}",
                    compra.getIdCompra(), idAsistente, idEntrada, cantidad);

            // Aquí se podría, opcionalmente, devolver información resumen de la venta
            // o enviar notificaciones, etc.
        } catch (Exception e) {
            // Manejo de excepciones y rollback
            handleException(e, tx, "registrar venta para Asistente ID " + idAsistente + ", Entrada ID " + idEntrada);
            // Relanzar excepción mapeada
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda (Helpers) ---
    /**
     * Manejador genérico de excepciones para métodos de servicio. Realiza
     * rollback si hay transacción activa y loggea el error. (Reutilizado de
     * otros servicios).
     *
     * @param e La excepción capturada.
     * @param tx La transacción activa (puede ser null).
     * @param action Descripción de la acción que falló.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("Rollback de transacción de {} realizado.", action);
            } catch (Exception rbEx) {
                log.error("Error durante el rollback de {}: {}", action, rbEx.getMessage(), rbEx);
            }
        }
    }

    /**
     * Cierra el EntityManager si está abierto. (Reutilizado de otros
     * servicios).
     *
     * @param em El EntityManager a cerrar.
     */
    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    /**
     * Mapea excepciones técnicas a excepciones de negocio o RuntimeException.
     * (Reutilizado de otros servicios).
     *
     * @param e La excepción original.
     * @return La excepción mapeada o una RuntimeException genérica.
     */
    private RuntimeException mapException(Exception e) {
        // Asegurarse de incluir todas las excepciones personalizadas relevantes
        if (e instanceof AsistenteNotFoundException
                || e instanceof EntradaNotFoundException
                || e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException
                || e instanceof FestivalNoPublicadoException // Específica de venta
                || e instanceof StockInsuficienteException // Específica de venta
                || e instanceof EntradaAsignadaNotFoundException
                || e instanceof IllegalArgumentException
                || e instanceof SecurityException
                || e instanceof IllegalStateException
                || e instanceof PersistenceException
                || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        // Para otras excepciones no esperadas, envolvemos en RuntimeException
        return new RuntimeException("Error inesperado en la capa de servicio Venta: " + e.getMessage(), e);
    }
}
