package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*; // Importar todas las entidades
import com.daw2edudiego.beatpasstfg.repository.*; // Importar todos los repositorios
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import com.daw2edudiego.beatpasstfg.util.QRCodeUtil; // Importar utilidad QR

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType; // Para bloqueo pesimista (opcional)
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de VentaService. Orquesta el proceso de registro de venta,
 * generación de entradas y actualización de stock.
 */
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    // Inyección de dependencias (manual)
    private final AsistenteRepository asistenteRepository;
    private final EntradaRepository entradaRepository;
    private final CompraRepository compraRepository;
    private final CompraEntradaRepository compraEntradaRepository;
    private final EntradaAsignadaRepository entradaAsignadaRepository;
    private final FestivalRepository festivalRepository; // Necesario para verificar estado festival

    public VentaServiceImpl() {
        this.asistenteRepository = new AsistenteRepositoryImpl();
        this.entradaRepository = new EntradaRepositoryImpl();
        this.compraRepository = new CompraRepositoryImpl();
        this.compraEntradaRepository = new CompraEntradaRepositoryImpl();
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
    }

    @Override
    public void registrarVenta(Integer idAsistente, Integer idEntrada, int cantidad) {
        log.info("Service: Iniciando registro de venta - Asistente ID: {}, Entrada ID: {}, Cantidad: {}",
                idAsistente, idEntrada, cantidad);

        if (idAsistente == null || idEntrada == null) {
            throw new IllegalArgumentException("ID de asistente y ID de entrada son requeridos.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Buscar Asistente
            Asistente asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));

            // 2. Buscar Tipo de Entrada y bloquearla para actualizar stock (Bloqueo Pesimista)
            // Usar LockModeType.PESSIMISTIC_WRITE para evitar condiciones de carrera con el stock
            Entrada entrada = em.find(Entrada.class, idEntrada, LockModeType.PESSIMISTIC_WRITE);
            if (entrada == null) {
                throw new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada);
            }
            log.debug("Entrada encontrada: ID {}, Tipo: {}, Stock actual: {}", entrada.getIdEntrada(), entrada.getTipo(), entrada.getStock());

            // 3. Verificar Festival asociado
            Festival festival = entrada.getFestival();
            if (festival == null) {
                // Esto indica un problema de integridad de datos
                log.error("La entrada ID {} no tiene un festival asociado.", idEntrada);
                throw new IllegalStateException("Inconsistencia de datos: La entrada no está asociada a ningún festival.");
            }
            log.debug("Festival asociado: ID {}, Nombre: {}, Estado: {}", festival.getIdFestival(), festival.getNombre(), festival.getEstado());

            // 4. Validar Estado del Festival
            if (festival.getEstado() != EstadoFestival.PUBLICADO) {
                log.warn("Intento de compra para festival ID {} que no está PUBLICADO (Estado: {})", festival.getIdFestival(), festival.getEstado());
                throw new FestivalNoPublicadoException("No se pueden comprar entradas para el festival '" + festival.getNombre() + "' porque no está publicado.");
            }

            // 5. Validar Stock
            if (entrada.getStock() < cantidad) {
                log.warn("Stock insuficiente para entrada ID {}. Solicitado: {}, Disponible: {}", idEntrada, cantidad, entrada.getStock());
                throw new StockInsuficienteException("No hay suficiente stock para el tipo de entrada '" + entrada.getTipo() + "'. Disponible: " + entrada.getStock());
            }

            // 6. Crear Compra
            Compra compra = new Compra();
            compra.setAsistente(asistente);
            // Calcular total (precio unitario de la entrada * cantidad)
            BigDecimal totalCompra = entrada.getPrecio().multiply(new BigDecimal(cantidad));
            compra.setTotal(totalCompra);
            compra = compraRepository.save(em, compra); // Guardar para obtener ID
            log.debug("Compra creada con ID: {}", compra.getIdCompra());

            // 7. Crear CompraEntrada (Detalle)
            CompraEntrada compraEntrada = new CompraEntrada();
            compraEntrada.setCompra(compra);
            compraEntrada.setEntrada(entrada);
            compraEntrada.setCantidad(cantidad);
            compraEntrada.setPrecioUnitario(entrada.getPrecio()); // Guardar precio histórico
            compraEntrada = compraEntradaRepository.save(em, compraEntrada); // Guardar para obtener ID
            log.debug("CompraEntrada creada con ID: {}", compraEntrada.getIdCompraEntrada());

            // 8. Generar Entradas Asignadas y QRs
            List<EntradaAsignada> entradasGeneradas = new ArrayList<>();
            for (int i = 0; i < cantidad; i++) {
                EntradaAsignada entradaAsignada = new EntradaAsignada();
                entradaAsignada.setCompraEntrada(compraEntrada); // Vincular al detalle de compra
                entradaAsignada.setEstado(EstadoEntradaAsignada.ACTIVA); // Estado inicial

                // Generar contenido QR único
                String qrContent = QRCodeUtil.generarContenidoQrUnico();
                entradaAsignada.setCodigoQr(qrContent);
                log.trace("Generado QR para entrada {}: {}", i + 1, qrContent);

                // Guardar la entrada asignada
                entradaAsignada = entradaAsignadaRepository.save(em, entradaAsignada);
                entradasGeneradas.add(entradaAsignada);
            }
            log.debug("Generadas {} entradas asignadas para CompraEntrada ID: {}", cantidad, compraEntrada.getIdCompraEntrada());

            // 9. Actualizar Stock de la Entrada
            int nuevoStock = entrada.getStock() - cantidad;
            entrada.setStock(nuevoStock);
            entradaRepository.save(em, entrada); // Guardar la entrada con el stock actualizado
            log.info("Stock actualizado para Entrada ID {}. Nuevo stock: {}", idEntrada, nuevoStock);

            // 10. Commit
            tx.commit();
            log.info("Venta registrada exitosamente. Compra ID: {}", compra.getIdCompra());

            // Podríamos devolver algo aquí, como el ID de la compra o un DTO resumen
            // return compra.getIdCompra();
        } catch (Exception e) {
            log.error("Error registrando venta para Asistente ID {}, Entrada ID {}: {}", idAsistente, idEntrada, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                    log.warn("Rollback de transacción de venta realizado.");
                } catch (Exception rbEx) {
                    log.error("Error durante el rollback de la transacción de venta: {}", rbEx.getMessage(), rbEx);
                }
            }
            // Relanzar excepciones específicas o una genérica
            if (e instanceof AsistenteNotFoundException || e instanceof EntradaNotFoundException
                    || e instanceof FestivalNoPublicadoException || e instanceof StockInsuficienteException
                    || e instanceof IllegalArgumentException || e instanceof PersistenceException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado registrando la venta: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
