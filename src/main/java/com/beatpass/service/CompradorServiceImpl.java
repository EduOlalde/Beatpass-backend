package com.beatpass.service;

import com.beatpass.dto.CompradorDTO;
import com.beatpass.model.Comprador;
import com.beatpass.repository.CompradorRepository;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beatpass.mapper.CompradorMapper;

import java.util.Optional;

public class CompradorServiceImpl extends AbstractService implements CompradorService {

    private static final Logger log = LoggerFactory.getLogger(CompradorServiceImpl.class);

    private final CompradorRepository compradorRepository;
    private final CompradorMapper compradorMapper;

    @Inject
    public CompradorServiceImpl(CompradorRepository compradorRepository) {
        this.compradorRepository = compradorRepository;
        this.compradorMapper = CompradorMapper.INSTANCE;
    }

    @Override
    public Comprador obtenerOcrearCompradorPorEmail(String email, String nombre, String telefono) {
        log.info("Service: Obteniendo o creando comprador por email: {}", email);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio para obtener o crear un comprador.");
        }

        return executeTransactional(em -> {
            Optional<Comprador> existenteOpt = compradorRepository.findByEmail(em, email);

            if (existenteOpt.isPresent()) {
                Comprador compradorExistente = existenteOpt.get();
                // Opcional: Actualizar nombre/teléfono si el comprador ya existe y los datos son diferentes
                boolean changed = false;
                if (nombre != null && !nombre.isBlank() && !nombre.trim().equals(compradorExistente.getNombre())) {
                    compradorExistente.setNombre(nombre.trim());
                    changed = true;
                }
                if (telefono != null && !telefono.trim().equals(compradorExistente.getTelefono())) {
                    compradorExistente.setTelefono(telefono.trim());
                    changed = true;
                }
                if (changed) {
                    compradorExistente = compradorRepository.save(em, compradorExistente);
                    log.debug("Comprador existente con email {} actualizado.", email);
                } else {
                    log.debug("Comprador encontrado con email {}. No requiere actualización.", email);
                }
                return compradorExistente;
            } else {
                log.info("Comprador con email {} no encontrado, creando uno nuevo.", email);
                if (nombre == null || nombre.isBlank()) {
                    throw new IllegalArgumentException("El nombre es obligatorio al crear un nuevo comprador.");
                }
                Comprador nuevoComprador = new Comprador();
                nuevoComprador.setEmail(email.trim().toLowerCase());
                nuevoComprador.setNombre(nombre.trim());
                nuevoComprador.setTelefono(telefono != null ? telefono.trim() : null);
                nuevoComprador = compradorRepository.save(em, nuevoComprador);
                log.info("Nuevo comprador creado con ID {}", nuevoComprador.getIdComprador());
                return nuevoComprador;
            }
        }, "obtenerOcrearCompradorPorEmail " + email);
    }

    @Override
    public List<CompradorDTO> buscarCompradores(String searchTerm) {
        log.debug("Service: Buscando compradores con término: '{}'", searchTerm);
        return executeRead(em -> {
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
            return compradorMapper.toCompradorDTOList(compradores);
        }, "buscarCompradores " + searchTerm);
    }
}
