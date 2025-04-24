package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Asistente;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para la entidad {@link Asistente}. Define
 * las operaciones de persistencia estándar para los asistentes. Las
 * implementaciones de esta interfaz interactuarán con la base de datos,
 * generalmente utilizando JPA EntityManager.
 *
 * @author Eduardo Olalde
 */
public interface AsistenteRepository {

    /**
     * Guarda (crea o actualiza) una entidad Asistente en la base de datos. Si
     * el asistente tiene un ID nulo, se realiza una operación de persistencia
     * (creación). Si el asistente tiene un ID no nulo, se realiza una operación
     * de merge (actualización).
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa. El EntityManager proporcionado debe formar parte de esa
     * transacción.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param asistente La entidad Asistente a guardar. No debe ser nula.
     * @return La entidad Asistente guardada o actualizada (puede ser una
     * instancia diferente si se hizo merge).
     * @throws IllegalArgumentException si el asistente es nulo.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la persistencia (ej: email duplicado).
     */
    Asistente save(EntityManager em, Asistente asistente);

    /**
     * Busca un Asistente por su identificador único (clave primaria).
     *
     * @param em El EntityManager activo.
     * @param id El ID del asistente a buscar.
     * @return Un {@link Optional} que contiene el Asistente si se encuentra, o
     * un Optional vacío si no se encuentra o si el ID es nulo.
     */
    Optional<Asistente> findById(EntityManager em, Integer id);

    /**
     * Busca un Asistente por su dirección de correo electrónico, que se asume
     * única.
     *
     * @param em El EntityManager activo.
     * @param email El email del asistente a buscar.
     * @return Un {@link Optional} que contiene el Asistente si se encuentra, o
     * un Optional vacío si no se encuentra o si el email es nulo o vacío.
     */
    Optional<Asistente> findByEmail(EntityManager em, String email);

    /**
     * Busca y devuelve una lista de todos los asistentes únicos que tienen al
     * menos una {@link com.daw2edudiego.beatpasstfg.model.EntradaAsignada} para
     * un festival específico. La consulta navega desde Asistente a
     * EntradaAsignada, luego a CompraEntrada, luego a Entrada y finalmente
     * filtra por el ID del Festival asociado a la Entrada.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del festival para el cual buscar asistentes.
     * @return Una lista (posiblemente vacía) de entidades Asistente únicas. La
     * lista estará ordenada por nombre. Devuelve una lista vacía si el
     * idFestival es nulo o si ocurre un error.
     */
    List<Asistente> findAsistentesByFestivalId(EntityManager em, Integer idFestival);

    /**
     * Busca todos los asistentes registrados en el sistema. ¡Precaución! Puede
     * devolver una gran cantidad de datos. Usar con cuidado o implementar
     * paginación.
     *
     * @param em El EntityManager activo.
     * @return Una lista con todos los asistentes.
     */
    List<Asistente> findAll(EntityManager em); // Método añadido para completitud

    /**
     * Elimina un asistente de la base de datos.
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa. Considerar las implicaciones de las relaciones en cascada (ej:
     * borrar asistente podría borrar sus compras).
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param asistente El asistente a eliminar. No debe ser nulo.
     * @throws IllegalArgumentException si el asistente es nulo.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la eliminación.
     */
    void delete(EntityManager em, Asistente asistente); // Método añadido para completitud

}
