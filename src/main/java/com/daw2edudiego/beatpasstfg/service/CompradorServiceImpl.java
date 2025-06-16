package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.model.Comprador;
import com.daw2edudiego.beatpasstfg.repository.CompradorRepository;
import com.daw2edudiego.beatpasstfg.repository.CompradorRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CompradorServiceImpl implements CompradorService {

    private static final Logger log = LoggerFactory.getLogger(CompradorServiceImpl.class);
    private final CompradorRepository compradorRepository;

    public CompradorServiceImpl() {
        this.compradorRepository = new CompradorRepositoryImpl();
    }

    @Override
    public Comprador obtenerOcrearCompradorPorEmail(String email, String nombre, String telefono) {
        log.info("Service: Obteniendo o creando comprador por email: {}", email);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio para obtener o crear un comprador.");
        }

        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = null;
        try {
            Optional<Comprador> existenteOpt = compradorRepository.findByEmail(em, email);

            if (existenteOpt.isPresent()) {
                log.debug("Comprador encontrado con email {}", email);
                return existenteOpt.get();
            } else {
                log.info("Comprador con email {} no encontrado, creando uno nuevo.", email);
                if (nombre == null || nombre.isBlank()) {
                    throw new IllegalArgumentException("El nombre es obligatorio al crear un nuevo comprador.");
                }
                tx = em.getTransaction();
                tx.begin();
                Comprador nuevoComprador = new Comprador();
                nuevoComprador.setEmail(email.trim().toLowerCase());
                nuevoComprador.setNombre(nombre.trim());
                nuevoComprador.setTelefono(telefono != null ? telefono.trim() : null);
                nuevoComprador = compradorRepository.save(em, nuevoComprador);
                tx.commit();
                log.info("Nuevo comprador creado con ID {}", nuevoComprador.getIdComprador());
                return nuevoComprador;
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Error en obtenerOcrearCompradorPorEmail para email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error inesperado procesando el comprador.", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
