package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Consumo;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para la entidad {@link Consumo}. Define las
 * operaciones de persistencia para los registros de consumo cashless.
 *
 * @author Eduardo Olalde
 */
public interface ConsumoRepository {

    /**
     * Guarda un nuevo registro de consumo en la base de datos. Se asume que la
     * entidad Consumo ya tiene asociadas las entidades
     * {@link com.daw2edudiego.beatpasstfg.model.PulseraNFC} y
     * {@link com.daw2edudiego.beatpasstfg.model.Festival} correspondientes.
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param consumo La entidad Consumo a guardar. No debe ser nula, ni sus
     * relaciones clave.
     * @return La entidad Consumo guardada, con su ID asignado.
     * @throws IllegalArgumentException si consumo o sus relaciones clave son
     * nulas.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la persistencia.
     */
    Consumo save(EntityManager em, Consumo consumo);

    /**
     * Busca un registro de consumo por su identificador único (clave primaria).
     *
     * @param em El EntityManager activo.
     * @param id El ID del consumo a buscar.
     * @return Un {@link Optional} que contiene el Consumo si se encuentra, o un
     * Optional vacío si no se encuentra o si el ID es nulo.
     */
    Optional<Consumo> findById(EntityManager em, Integer id);

    /**
     * Busca y devuelve todos los consumos asociados a una
     * {@link com.daw2edudiego.beatpasstfg.model.PulseraNFC} específica. Los
     * resultados se ordenan por fecha de forma descendente (los más recientes
     * primero).
     *
     * @param em El EntityManager activo.
     * @param idPulsera El ID de la PulseraNFC cuyos consumos se quieren
     * obtener.
     * @return Una lista (posiblemente vacía) de entidades Consumo asociadas a
     * la pulsera dada. Devuelve una lista vacía si el idPulsera es nulo o si
     * ocurre un error.
     */
    List<Consumo> findByPulseraId(EntityManager em, Integer idPulsera);

    /**
     * Busca y devuelve todos los consumos asociados a un
     * {@link com.daw2edudiego.beatpasstfg.model.Festival} específico. Los
     * resultados se ordenan por fecha de forma descendente.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival cuyos consumos se quieren obtener.
     * @return Una lista (posiblemente vacía) de entidades Consumo asociadas al
     * festival dado. Devuelve una lista vacía si el idFestival es nulo o si
     * ocurre un error.
     */
    List<Consumo> findByFestivalId(EntityManager em, Integer idFestival);

    // Podrían añadirse otros métodos como:
    // List<Consumo> findByFestivalIdAndPulseraId(EntityManager em, Integer idFestival, Integer idPulsera);
    // BigDecimal calculateTotalConsumoByPulseraId(EntityManager em, Integer idPulsera);
    // void delete(EntityManager em, Consumo consumo);
}
