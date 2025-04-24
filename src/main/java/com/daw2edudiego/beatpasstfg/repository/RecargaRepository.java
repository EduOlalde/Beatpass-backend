package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Recarga;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para la entidad {@link Recarga}. Define las
 * operaciones de persistencia para los registros de recarga de saldo en
 * pulseras.
 *
 * @author Eduardo Olalde
 */
public interface RecargaRepository {

    /**
     * Guarda un nuevo registro de recarga en la base de datos. Se asume que la
     * entidad Recarga ya tiene asociada la
     * {@link com.daw2edudiego.beatpasstfg.model.PulseraNFC} correspondiente y
     * un monto.
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param recarga La entidad Recarga a guardar. No debe ser nula, ni su
     * PulseraNFC o monto.
     * @return La entidad Recarga guardada, con su ID asignado.
     * @throws IllegalArgumentException si recarga o sus campos clave son nulos.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la persistencia.
     */
    Recarga save(EntityManager em, Recarga recarga);

    /**
     * Busca un registro de recarga por su identificador único (clave primaria).
     *
     * @param em El EntityManager activo.
     * @param id El ID de la recarga a buscar.
     * @return Un {@link Optional} que contiene la Recarga si se encuentra, o un
     * Optional vacío si no se encuentra o si el ID es nulo.
     */
    Optional<Recarga> findById(EntityManager em, Integer id);

    /**
     * Busca y devuelve todos los registros de recarga asociados a una
     * {@link com.daw2edudiego.beatpasstfg.model.PulseraNFC} específica. Los
     * resultados se ordenan por fecha de forma descendente (las más recientes
     * primero).
     *
     * @param em El EntityManager activo.
     * @param idPulsera El ID de la PulseraNFC cuyas recargas se quieren
     * obtener.
     * @return Una lista (posiblemente vacía) de entidades Recarga asociadas a
     * la pulsera dada. Devuelve una lista vacía si el idPulsera es nulo o si
     * ocurre un error.
     */
    List<Recarga> findByPulseraId(EntityManager em, Integer idPulsera);

    // Podrían añadirse otros métodos como:
    // List<Recarga> findByUsuarioCajeroId(EntityManager em, Integer idUsuarioCajero);
    // BigDecimal calculateTotalRecargaByPulseraId(EntityManager em, Integer idPulsera);
    // void delete(EntityManager em, Recarga recarga);
}
