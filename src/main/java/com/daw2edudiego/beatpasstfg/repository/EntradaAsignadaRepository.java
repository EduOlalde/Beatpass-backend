package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.EntradaAsignada;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para la entidad {@link EntradaAsignada},
 * que representa una entrada individual y única. Define las operaciones de
 * persistencia para estas entradas.
 *
 * @author Eduardo Olalde
 */
public interface EntradaAsignadaRepository {

    /**
     * Guarda (crea o actualiza) una entidad EntradaAsignada. Si la entrada
     * tiene ID nulo, se persiste. Si tiene ID, se actualiza (merge). Se asume
     * que la entidad tiene asociada su
     * {@link com.daw2edudiego.beatpasstfg.model.CompraEntrada} y un código QR
     * único generado.
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param entradaAsignada La entidad EntradaAsignada a guardar. No debe ser
     * nula, ni su CompraEntrada o codigoQr.
     * @return La entidad EntradaAsignada guardada o actualizada.
     * @throws IllegalArgumentException si entradaAsignada o sus campos clave
     * son nulos.
     * @throws jakarta.persistence.PersistenceException si ocurre un error (ej:
     * QR duplicado).
     */
    EntradaAsignada save(EntityManager em, EntradaAsignada entradaAsignada);

    /**
     * Busca una EntradaAsignada por su identificador único (clave primaria).
     *
     * @param em El EntityManager activo.
     * @param id El ID de la entrada asignada a buscar.
     * @return Un {@link Optional} que contiene la EntradaAsignada si se
     * encuentra, o un Optional vacío si no se encuentra o si el ID es nulo.
     */
    Optional<EntradaAsignada> findById(EntityManager em, Integer id);

    /**
     * Busca una EntradaAsignada por su código QR, que debe ser único.
     *
     * @param em El EntityManager activo.
     * @param codigoQr El código QR de la entrada a buscar.
     * @return Un {@link Optional} que contiene la EntradaAsignada si se
     * encuentra, o un Optional vacío si no se encuentra o si el código QR es
     * nulo o vacío.
     */
    Optional<EntradaAsignada> findByCodigoQr(EntityManager em, String codigoQr);

    /**
     * Busca y devuelve todas las entradas asignadas que se generaron a partir
     * de un {@link com.daw2edudiego.beatpasstfg.model.CompraEntrada} (detalle
     * de compra) específico.
     *
     * @param em El EntityManager activo.
     * @param idCompraEntrada El ID del CompraEntrada origen.
     * @return Una lista (posiblemente vacía) de entidades EntradaAsignada
     * generadas por ese detalle. Devuelve una lista vacía si el idCompraEntrada
     * es nulo o si ocurre un error.
     */
    List<EntradaAsignada> findByCompraEntradaId(EntityManager em, Integer idCompraEntrada);

    /**
     * Busca y devuelve todas las entradas asignadas que pertenecen a un
     * {@link com.daw2edudiego.beatpasstfg.model.Festival} específico. La
     * consulta navega a través de CompraEntrada y Entrada para llegar al
     * festival.
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del Festival cuyas entradas asignadas se quieren
     * obtener.
     * @return Una lista (posiblemente vacía) de entidades EntradaAsignada
     * asociadas al festival. Devuelve una lista vacía si el idFestival es nulo
     * o si ocurre un error.
     */
    List<EntradaAsignada> findByFestivalId(EntityManager em, Integer idFestival);

    // Podrían añadirse otros métodos como:
    // List<EntradaAsignada> findByAsistenteId(EntityManager em, Integer idAsistente);
    // List<EntradaAsignada> findByFestivalIdAndEstado(EntityManager em, Integer idFestival, EstadoEntradaAsignada estado);
    // void delete(EntityManager em, EntradaAsignada entradaAsignada);
}
