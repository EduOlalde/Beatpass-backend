package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.PulseraNFC;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para la entidad {@link PulseraNFC}. Define
 * las operaciones de persistencia estándar para las pulseras NFC.
 *
 * @author Eduardo Olalde
 */
public interface PulseraNFCRepository {

    /**
     * Guarda (crea o actualiza) una entidad PulseraNFC. Si la pulsera tiene ID
     * nulo, se persiste. Si tiene ID, se actualiza (merge).
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param pulsera La entidad PulseraNFC a guardar. No debe ser nula, ni su
     * codigoUid.
     * @return La entidad PulseraNFC guardada o actualizada.
     * @throws IllegalArgumentException si la pulsera o su codigoUid son nulos.
     * @throws jakarta.persistence.PersistenceException si ocurre un error (ej:
     * UID duplicado).
     */
    PulseraNFC save(EntityManager em, PulseraNFC pulsera);

    /**
     * Busca una PulseraNFC por su identificador único de base de datos (clave
     * primaria).
     *
     * @param em El EntityManager activo.
     * @param id El ID de la pulsera a buscar.
     * @return Un {@link Optional} que contiene la PulseraNFC si se encuentra, o
     * un Optional vacío si no se encuentra o si el ID es nulo.
     */
    Optional<PulseraNFC> findById(EntityManager em, Integer id);

    /**
     * Busca una PulseraNFC por su código UID (identificador único del chip
     * NFC), que debe ser único en el sistema.
     *
     * @param em El EntityManager activo.
     * @param codigoUid El código UID de la pulsera a buscar.
     * @return Un {@link Optional} que contiene la PulseraNFC si se encuentra, o
     * un Optional vacío si no se encuentra o si el código UID es nulo o vacío.
     */
    Optional<PulseraNFC> findByCodigoUid(EntityManager em, String codigoUid);

    /**
     * Busca la PulseraNFC que está asociada a una
     * {@link com.daw2edudiego.beatpasstfg.model.EntradaAsignada} específica.
     * Dado que la relación es 1:1 (una entrada solo puede tener una pulsera
     * asociada), debería encontrarse como máximo una pulsera.
     *
     * @param em El EntityManager activo.
     * @param idEntradaAsignada El ID de la EntradaAsignada cuya pulsera
     * asociada se busca.
     * @return Un {@link Optional} que contiene la PulseraNFC si se encuentra
     * una asociación, o un Optional vacío si no hay ninguna pulsera asociada a
     * esa entrada o si el ID es nulo.
     */
    Optional<PulseraNFC> findByEntradaAsignadaId(EntityManager em, Integer idEntradaAsignada);

    /**
     * Busca y devuelve todas las pulseras NFC que están asociadas (a través de
     * EntradaAsignada) a un {@link com.daw2edudiego.beatpasstfg.model.Festival}
     * específico. La consulta navega PulseraNFC -> EntradaAsignada ->
     * CompraEntrada -> Entrada -> Festival.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival cuyas pulseras asociadas se quieren
     * obtener.
     * @return Una lista (posiblemente vacía) de entidades PulseraNFC asociadas
     * al festival dado. Devuelve una lista vacía si el idFestival es nulo o si
     * ocurre un error.
     */
    List<PulseraNFC> findByFestivalId(EntityManager em, Integer idFestival);

    // Podrían añadirse otros métodos como:
    // List<PulseraNFC> findAll(EntityManager em);
    // List<PulseraNFC> findByActiva(EntityManager em, boolean activa);
    // void delete(EntityManager em, PulseraNFC pulsera);
}
