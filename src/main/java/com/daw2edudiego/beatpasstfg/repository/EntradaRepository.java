package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Entrada;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para la entidad {@link Entrada}, que
 * representa un tipo de entrada para un festival. Define las operaciones de
 * persistencia estándar para los tipos de entrada.
 *
 * @author Eduardo Olalde
 */
public interface EntradaRepository {

    /**
     * Guarda (crea o actualiza) una entidad Entrada (tipo de entrada). Si la
     * entrada tiene ID nulo, se persiste. Si tiene ID, se actualiza (merge).
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param entrada La entidad Entrada a guardar. No debe ser nula.
     * @return La entidad Entrada guardada o actualizada.
     * @throws IllegalArgumentException si la entrada es nula.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la persistencia.
     */
    Entrada save(EntityManager em, Entrada entrada);

    /**
     * Busca un tipo de entrada por su identificador único (clave primaria).
     *
     * @param em El EntityManager activo.
     * @param id El ID del tipo de entrada a buscar.
     * @return Un {@link Optional} que contiene la Entrada si se encuentra, o un
     * Optional vacío si no se encuentra o si el ID es nulo.
     */
    Optional<Entrada> findById(EntityManager em, Integer id);

    /**
     * Busca y devuelve todos los tipos de entrada asociados a un
     * {@link com.daw2edudiego.beatpasstfg.model.Festival} específico. Los
     * resultados se ordenan por el tipo/nombre de la entrada.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival cuyos tipos de entrada se quieren
     * obtener.
     * @return Una lista (posiblemente vacía) de entidades Entrada asociadas al
     * festival dado. Devuelve una lista vacía si el idFestival es nulo o si
     * ocurre un error.
     */
    List<Entrada> findByFestivalId(EntityManager em, Integer idFestival);

    /**
     * Elimina un tipo de entrada por su ID. Busca la entidad y, si existe, la
     * marca para eliminar.
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa. ¡Precaución! Eliminar un tipo de entrada puede fallar si existen
     * {@link com.daw2edudiego.beatpasstfg.model.CompraEntrada} asociadas,
     * debido a restricciones de clave foránea, a menos que se configure el
     * borrado en cascada (lo cual podría no ser deseable para mantener el
     * historial).
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param id El ID del tipo de entrada a eliminar.
     * @return {@code true} si la entidad fue encontrada y marcada para
     * eliminar, {@code false} si no se encontró.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la eliminación (ej: violación de FK).
     */
    boolean deleteById(EntityManager em, Integer id);

    // Podrían añadirse otros métodos como:
    // Optional<Entrada> findByFestivalIdAndTipo(EntityManager em, Integer idFestival, String tipo);
    // List<Entrada> findAll(EntityManager em);
}
