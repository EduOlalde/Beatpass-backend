package com.beatpass.repository;

import com.beatpass.model.Asistente;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Asistente. Define las operaciones de
 * persistencia estándar.
 */
public interface AsistenteRepository {

    /**
     * Guarda (crea o actualiza) una entidad Asistente. Debe ejecutarse dentro
     * de una transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param asistente La entidad Asistente a guardar.
     * @return La entidad Asistente guardada o actualizada.
     */
    Asistente save(EntityManager em, Asistente asistente);

    /**
     * Busca un Asistente por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID del asistente a buscar.
     * @return Un Optional con el Asistente si se encuentra, o vacío.
     */
    Optional<Asistente> findById(EntityManager em, Integer id);

    /**
     * Busca un Asistente por su email (único).
     *
     * @param em El EntityManager activo.
     * @param email El email del asistente a buscar.
     * @return Un Optional con el Asistente si se encuentra, o vacío.
     */
    Optional<Asistente> findByEmail(EntityManager em, String email);

    /**
     * Busca asistentes únicos que tienen entradas para un festival específico.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del festival.
     * @return Una lista (posiblemente vacía) de Asistentes únicos.
     */
    List<Asistente> findAsistentesByFestivalId(EntityManager em, Integer idFestival);

    /**
     * Busca todos los asistentes. Usar con precaución.
     *
     * @param em El EntityManager activo.
     * @return Una lista con todos los asistentes.
     */
    List<Asistente> findAll(EntityManager em);

    /**
     * Elimina un asistente. Debe ejecutarse dentro de una transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param asistente El asistente a eliminar.
     */
    void delete(EntityManager em, Asistente asistente);

    /**
     * Busca los detalles de los asistentes para un festival específico,
     * incluyendo información de sus entradas y pulseras asociadas.
     * <p>
     * Esta consulta está optimizada para devolver una lista plana de
     * resultados, uniendo Asistente, Entrada, Festival y PulseraNFC en una sola
     * llamada a la base de datos para evitar el problema N+1.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del festival para el cual se buscan los
     * asistentes.
     * @return Una lista de arrays de objetos (List<Object[]>), donde cada array
     * representa una combinación única de asistente-festival y contiene los
     * siguientes datos en orden:
     * <ul>
     * <li>[0]: {@link Integer} - ID del Asistente (a.idAsistente)</li>
     * <li>[1]: {@link String} - Nombre del Asistente (a.nombre)</li>
     * <li>[2]: {@link String} - Email del Asistente (a.email)</li>
     * <li>[3]: {@link String} - Teléfono del Asistente (a.telefono)</li>
     * <li>[4]: {@link java.time.LocalDateTime} - Fecha de Creación del
     * Asistente (a.fechaCreacion)</li>
     * <li>[5]: {@link String} - Nombre del Festival (f.nombre)</li>
     * <li>[6]: {@link String} - UID de la Pulsera (p.codigoUid), puede ser
     * null</li>
     * </ul>
     */
    List<Object[]> findAsistenteDetailsByFestivalId(EntityManager em, Integer idFestival);

}
