package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompradorDTO;
import com.daw2edudiego.beatpasstfg.model.Comprador;
import com.daw2edudiego.beatpasstfg.repository.CompradorRepository;
import com.daw2edudiego.beatpasstfg.repository.CompradorRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public List<CompradorDTO> buscarCompradores(String searchTerm) {
        log.debug("Service: Buscando compradores con término: '{}'", searchTerm);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Comprador> compradores;
            if (searchTerm == null || searchTerm.isBlank()) {
                compradores = em.createQuery("SELECT c FROM Comprador c ORDER BY c.nombre", Comprador.class).getResultList();
            } else {
                TypedQuery<Comprador> query = em.createQuery(
                        "SELECT c FROM Comprador c WHERE lower(c.nombre) LIKE :term OR lower(c.email) LIKE :term ORDER BY c.nombre", Comprador.class);
                query.setParameter("term", "%" + searchTerm.toLowerCase() + "%");
                compradores = query.getResultList();
            }
            log.info("Encontrados {} compradores para el término '{}'", compradores.size(), searchTerm);
            return compradores.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error buscando compradores con término '{}': {}", searchTerm, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    private CompradorDTO mapEntityToDto(Comprador c) {
        if (c == null) {
            return null;
        }
        CompradorDTO dto = new CompradorDTO();
        dto.setIdComprador(c.getIdComprador());
        dto.setNombre(c.getNombre());
        dto.setEmail(c.getEmail());
        dto.setTelefono(c.getTelefono());
        dto.setFechaCreacion(c.getFechaCreacion());
        return dto;
    }

}
