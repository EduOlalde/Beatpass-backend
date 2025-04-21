package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.AsistenteDTO;
import com.daw2edudiego.beatpasstfg.exception.AsistenteNotFoundException; // Necesaria
import com.daw2edudiego.beatpasstfg.model.Asistente; // Necesario para crear/actualizar

import java.util.List;
import java.util.Optional;

/**
 * Interfaz para la lógica de negocio relacionada con Asistentes.
 */
public interface AsistenteService {

    /**
     * Obtiene todos los asistentes registrados. (Potencialmente restringido a
     * ADMIN).
     *
     * @return Lista de AsistenteDTO.
     */
    List<AsistenteDTO> obtenerTodosLosAsistentes();

    /**
     * Obtiene un asistente por su ID.
     *
     * @param idAsistente ID del asistente.
     * @return Optional con el AsistenteDTO si existe.
     */
    Optional<AsistenteDTO> obtenerAsistentePorId(Integer idAsistente);

    /**
     * Busca un asistente por su email. Si no existe, lo crea con los datos
     * proporcionados. Útil para procesos de compra o nominación donde se
     * identifica al asistente por email. Debe ser transaccional si implica
     * creación.
     *
     * @param email Email del asistente (clave única).
     * @param nombre Nombre a usar si se crea el asistente.
     * @param telefono Teléfono opcional a usar si se crea.
     * @return La entidad Asistente (existente o recién creada).
     * @throws IllegalArgumentException si el email es nulo o inválido, o si
     * falta el nombre al crear.
     */
    Asistente obtenerOcrearAsistentePorEmail(String email, String nombre, String telefono);

    /**
     * Busca asistentes cuyo nombre o email contenga el término de búsqueda.
     * Útil para funcionalidades de búsqueda en paneles. (Potencialmente
     * restringido a ADMIN o con filtros por festival para promotor).
     *
     * @param searchTerm Término a buscar en nombre o email.
     * @return Lista de AsistenteDTO que coinciden.
     */
    List<AsistenteDTO> buscarAsistentes(String searchTerm);

    /**
     * Actualiza los datos de un asistente existente. No permite cambiar el
     * email.
     *
     * @param idAsistente ID del asistente a actualizar.
     * @param asistenteDTO DTO con los nuevos datos (nombre, telefono).
     * @return El DTO del asistente actualizado.
     * @throws AsistenteNotFoundException si el asistente no existe.
     * @throws IllegalArgumentException si los datos del DTO son inválidos.
     * @throws RuntimeException si ocurre un error inesperado.
     */
    AsistenteDTO actualizarAsistente(Integer idAsistente, AsistenteDTO asistenteDTO);

    /**
     * Obtiene la lista de asistentes únicos que tienen entradas para un
     * festival específico. Verifica que el promotor solicitante sea el dueño
     * del festival.
     *
     * @param idFestival ID del festival.
     * @param idPromotor ID del promotor que realiza la consulta.
     * @return Lista de AsistenteDTO asociados al festival.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws SecurityException si el promotor no es dueño del festival.
     * @throws RuntimeException si ocurre un error inesperado.
     */
    List<AsistenteDTO> obtenerAsistentesPorFestival(Integer idFestival, Integer idPromotor); 

    // Podrían añadirse métodos para actualizar o eliminar asistentes si fuera necesario.
    // AsistenteDTO actualizarAsistente(Integer idAsistente, AsistenteDTO asistenteDTO);
    // void eliminarAsistente(Integer idAsistente);
}
