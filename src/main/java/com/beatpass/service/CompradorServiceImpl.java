package com.beatpass.service;

import com.beatpass.dto.CompradorDTO;
import com.beatpass.mapper.CompradorMapper;
import com.beatpass.model.Comprador;
import com.beatpass.repository.CompradorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de Compradores. Proporciona la
 * lógica de negocio para obtener, crear y buscar entidades de tipo Comprador.
 */
@ApplicationScoped
public class CompradorServiceImpl implements CompradorService {

    private static final Logger log = LoggerFactory.getLogger(CompradorServiceImpl.class);

    @Inject
    private CompradorRepository compradorRepository;

    @Inject
    private CompradorMapper compradorMapper;

    @Override
    @Transactional
    public Comprador obtenerOcrearCompradorPorEmail(String email, String nombre, String telefono) {
        log.info("Service: Obteniendo o creando comprador por email: {}", email);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio para obtener o crear un comprador.");
        }

        Optional<Comprador> existenteOpt = compradorRepository.findByEmail(email);

        if (existenteOpt.isPresent()) {
            Comprador compradorExistente = existenteOpt.get();
            boolean changed = false;
            if (nombre != null && !nombre.isBlank() && !nombre.trim().equals(compradorExistente.getNombre())) {
                compradorExistente.setNombre(nombre.trim());
                changed = true;
            }
            if (telefono != null && !telefono.isBlank() && !telefono.trim().equals(compradorExistente.getTelefono())) {
                compradorExistente.setTelefono(telefono.trim());
                changed = true;
            }
            if (changed) {
                compradorExistente = compradorRepository.save(compradorExistente);
                log.debug("Comprador existente con email {} actualizado.", email);
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
            return compradorRepository.save(nuevoComprador);
        }
    }

    @Override
    public List<CompradorDTO> buscarCompradores(String searchTerm) {
        log.debug("Service: Buscando compradores con término: '{}'", searchTerm);
        // La lógica de búsqueda ahora está delegada al repositorio.
        List<Comprador> compradores = compradorRepository.searchByTerm(searchTerm);
        log.info("Encontrados {} compradores para el término '{}'", compradores.size(), searchTerm);
        return compradorMapper.toCompradorDTOList(compradores);
    }
}
