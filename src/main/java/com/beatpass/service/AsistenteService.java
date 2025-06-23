package com.beatpass.service;

import com.beatpass.dto.AsistenteDTO;
import com.beatpass.exception.AsistenteNotFoundException;
import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.model.Asistente;
import java.util.List;
import java.util.Optional;

/**
 * Define la lógica de negocio para la gestión de Asistentes.
 */
public interface AsistenteService {

    /**
     * Obtiene una lista de todos los asistentes registrados.
     *
     * @return Lista de AsistenteDTO.
     */
    List<AsistenteDTO> obtenerTodosLosAsistentes();

    /**
     * Obtiene la información de un asistente por su ID.
     *
     * @param idAsistente El ID del asistente.
     * @return Optional con AsistenteDTO si existe.
     */
    Optional<AsistenteDTO> obtenerAsistentePorId(Integer idAsistente);

    /**
     * Busca un asistente por email. Si no existe, lo crea. Es transaccional si
     * implica creación.
     *
     * @param email Email del asistente (obligatorio, único).
     * @param nombre Nombre (obligatorio si se crea).
     * @param telefono Teléfono (opcional).
     * @return La entidad Asistente (existente o nueva).
     * @throws IllegalArgumentException Si faltan datos obligatorios.
     */
    Asistente obtenerOcrearAsistentePorEmail(String email, String nombre, String telefono);

    /**
     * Busca asistentes por nombre o email que contengan el término.
     *
     * @param searchTerm Término de búsqueda (si es nulo/vacío, devuelve todos).
     * @return Lista de AsistenteDTO coincidentes.
     */
    List<AsistenteDTO> buscarAsistentes(String searchTerm);

    /**
     * Actualiza nombre y teléfono de un asistente existente. Email no
     * modificable. Es transaccional.
     *
     * @param idAsistente ID del asistente a actualizar.
     * @param asistenteDTO DTO con los nuevos datos (nombre obligatorio).
     * @return El AsistenteDTO actualizado.
     * @throws AsistenteNotFoundException Si no se encuentra el asistente.
     * @throws IllegalArgumentException Si faltan datos o son inválidos.
     */
    AsistenteDTO actualizarAsistente(Integer idAsistente, AsistenteDTO asistenteDTO);

    /**
     * Obtiene los asistentes únicos con entradas para un festival específico.
     * Verifica permisos del promotor sobre el festival.
     *
     * @param idFestival ID del festival.
     * @param idPromotor ID del promotor solicitante.
     * @return Lista de AsistenteDTO.
     * @throws FestivalNotFoundException si el festival no se encuentra.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    List<AsistenteDTO> obtenerAsistentesPorFestival(Integer idFestival, Integer idPromotor);

    List<AsistenteDTO> obtenerTodosLosAsistentesConFiltro(String searchTerm);

}
