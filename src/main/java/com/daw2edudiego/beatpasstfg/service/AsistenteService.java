package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.AsistenteDTO;
import com.daw2edudiego.beatpasstfg.exception.AsistenteNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.model.Asistente;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define la lógica de negocio y las operaciones relacionadas con
 * la gestión de Asistentes. Esta capa coordina las interacciones con los
 * repositorios y maneja las transacciones.
 *
 * @author Eduardo Olalde
 */
public interface AsistenteService {

    /**
     * Obtiene una lista de todos los asistentes registrados en el sistema. Esta
     * operación podría estar restringida a usuarios con rol ADMIN.
     *
     * @return Una lista de {@link AsistenteDTO}, posiblemente vacía.
     */
    List<AsistenteDTO> obtenerTodosLosAsistentes();

    /**
     * Obtiene la información de un asistente específico basado en su ID.
     *
     * @param idAsistente El ID del asistente a buscar.
     * @return Un {@link Optional} conteniendo el {@link AsistenteDTO} si el
     * asistente existe, de lo contrario, un Optional vacío.
     */
    Optional<AsistenteDTO> obtenerAsistentePorId(Integer idAsistente);

    /**
     * Busca un asistente por su dirección de correo electrónico. Si el
     * asistente no existe, lo crea utilizando el nombre y teléfono
     * proporcionados. Esta operación es útil durante procesos como la
     * nominación de entradas, donde se identifica al asistente por email pero
     * podría no existir previamente.
     * <p>
     * La operación es transaccional si implica la creación de un nuevo
     * asistente.
     * </p>
     *
     * @param email El correo electrónico del asistente (identificador único).
     * No puede ser nulo ni vacío.
     * @param nombre El nombre del asistente, requerido si se necesita crear uno
     * nuevo.
     * @param telefono El número de teléfono del asistente (opcional).
     * @return La entidad {@link Asistente} persistida (ya sea la existente o la
     * recién creada).
     * @throws IllegalArgumentException si el email es nulo/vacío, o si se
     * requiere crear un asistente y el nombre es nulo/vacío.
     * @throws RuntimeException si ocurre un error inesperado durante la
     * búsqueda o creación.
     */
    Asistente obtenerOcrearAsistentePorEmail(String email, String nombre, String telefono);

    /**
     * Busca asistentes cuyo nombre o correo electrónico contenga (ignorando
     * mayúsculas/minúsculas) el término de búsqueda proporcionado. Útil para
     * implementar funcionalidades de búsqueda en paneles de administración.
     * (Podría requerir filtros adicionales por rol o festival en un futuro).
     *
     * @param searchTerm El término a buscar. Si es nulo o vacío, puede devolver
     * todos los asistentes (o una lista vacía).
     * @return Una lista de {@link AsistenteDTO} que coinciden con el criterio
     * de búsqueda.
     */
    List<AsistenteDTO> buscarAsistentes(String searchTerm);

    /**
     * Actualiza los datos modificables (nombre, teléfono) de un asistente
     * existente. No permite modificar el correo electrónico, ya que actúa como
     * identificador. La operación es transaccional.
     *
     * @param idAsistente El ID del asistente a actualizar.
     * @param asistenteDTO El DTO que contiene los nuevos datos (principalmente
     * nombre y teléfono). El nombre no puede estar vacío.
     * @return El {@link AsistenteDTO} con los datos actualizados del asistente.
     * @throws AsistenteNotFoundException si no se encuentra un asistente con el
     * ID proporcionado.
     * @throws IllegalArgumentException si el idAsistente o el DTO son nulos, o
     * si el nombre en el DTO está vacío.
     * @throws RuntimeException si ocurre un error inesperado durante la
     * actualización.
     */
    AsistenteDTO actualizarAsistente(Integer idAsistente, AsistenteDTO asistenteDTO);

    /**
     * Obtiene la lista de asistentes únicos que poseen al menos una entrada
     * asignada para un festival específico. Antes de realizar la consulta,
     * verifica que el festival exista y que el usuario (promotor) que realiza
     * la solicitud sea el propietario de dicho festival.
     *
     * @param idFestival El ID del festival para el cual se buscan los
     * asistentes.
     * @param idPromotor El ID del usuario (promotor) que realiza la consulta
     * para verificación de permisos.
     * @return Una lista de {@link AsistenteDTO} que tienen entradas para el
     * festival especificado.
     * @throws FestivalNotFoundException si el festival no se encuentra.
     * @throws SecurityException si el promotor no tiene permisos sobre el
     * festival.
     * @throws IllegalArgumentException si idFestival o idPromotor son nulos.
     * @throws RuntimeException si ocurre un error inesperado.
     */
    List<AsistenteDTO> obtenerAsistentesPorFestival(Integer idFestival, Integer idPromotor);

    // Podrían añadirse métodos para eliminar asistentes si la lógica de negocio lo requiere:
    // void eliminarAsistente(Integer idAsistente, Integer idAdmin); // Requiere permisos de admin
}
