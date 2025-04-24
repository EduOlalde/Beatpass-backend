package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.Festival;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para la entidad {@link Festival}. Define
 * las operaciones de persistencia estándar para los festivales.
 *
 * @author Eduardo Olalde
 */
public interface FestivalRepository {

    /**
     * Guarda (crea o actualiza) una entidad Festival. Si el festival tiene ID
     * nulo, se persiste. Si tiene ID, se actualiza (merge).
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param festival El festival a guardar. No debe ser nulo.
     * @return La entidad Festival guardada o actualizada.
     * @throws IllegalArgumentException si el festival es nulo.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la persistencia.
     */
    Festival save(EntityManager em, Festival festival);

    /**
     * Busca un Festival por su identificador único (clave primaria).
     *
     * @param em El EntityManager activo.
     * @param id El ID del festival a buscar.
     * @return Un {@link Optional} que contiene el Festival si se encuentra, o
     * un Optional vacío si no se encuentra o si el ID es nulo.
     */
    Optional<Festival> findById(EntityManager em, Integer id);

    /**
     * Elimina un festival por su ID. Busca la entidad y, si existe, la marca
     * para eliminar.
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa. Considerar las relaciones en cascada (eliminar un festival podría
     * eliminar sus entradas, consumos, etc.).
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param id El ID del festival a eliminar.
     * @return {@code true} si la entidad fue encontrada y marcada para
     * eliminar, {@code false} si no se encontró.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la eliminación.
     */
    boolean deleteById(EntityManager em, Integer id);

    /**
     * Busca y devuelve todos los festivales registrados en el sistema. Los
     * resultados se ordenan por nombre. ¡Precaución! Puede devolver muchos
     * resultados en un sistema real. Considerar paginación.
     *
     * @param em El EntityManager activo.
     * @return Una lista (posiblemente vacía) con todos los festivales.
     */
    List<Festival> findAll(EntityManager em);

    /**
     * Busca y devuelve todos los festivales que se encuentran en un estado
     * específico. Los resultados se ordenan por fecha de inicio.
     *
     * @param em El EntityManager activo.
     * @param estado El {@link EstadoFestival} a buscar.
     * @return Una lista (posiblemente vacía) de festivales en el estado
     * especificado. Devuelve lista vacía si el estado es nulo o si ocurre un
     * error.
     */
    List<Festival> findByEstado(EntityManager em, EstadoFestival estado);

    /**
     * Busca y devuelve todos los festivales activos (estado PUBLICADO) cuyo
     * periodo (fechaInicio a fechaFin) se solapa con el rango de fechas
     * proporcionado. Los resultados se ordenan por fecha de inicio.
     *
     * @param em El EntityManager activo.
     * @param fechaDesde Fecha de inicio del rango de búsqueda.
     * @param fechaHasta Fecha de fin del rango de búsqueda.
     * @return Una lista (posiblemente vacía) de festivales activos que ocurren
     * (total o parcialmente) dentro del rango de fechas especificado. Devuelve
     * lista vacía si las fechas son nulas, inválidas o si ocurre un error.
     */
    List<Festival> findActivosEntreFechas(EntityManager em, LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Busca y devuelve todos los festivales gestionados por un promotor
     * específico. Los resultados se ordenan por fecha de inicio descendente
     * (los más recientes primero).
     *
     * @param em El EntityManager activo.
     * @param idPromotor El ID del
     * {@link com.daw2edudiego.beatpasstfg.model.Usuario} (promotor).
     * @return Una lista (posiblemente vacía) de festivales asociados al
     * promotor dado. Devuelve lista vacía si el idPromotor es nulo o si ocurre
     * un error.
     */
    List<Festival> findByPromotorId(EntityManager em, Integer idPromotor);

    // Podrían añadirse otros métodos como:
    // List<Festival> findByNombreContaining(EntityManager em, String nombre);
    // List<Festival> findByUbicacion(EntityManager em, String ubicacion);
}
