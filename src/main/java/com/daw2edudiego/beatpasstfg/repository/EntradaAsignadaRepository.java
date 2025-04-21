package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.EntradaAsignada;
import jakarta.persistence.EntityManager;
import java.util.Optional; // Importar Optional
import java.util.List; // Importar List

/**
 * Interfaz DAO para la entidad EntradaAsignada (entradas individuales).
 *
 * @author Eduardo Olalde
 */
public interface EntradaAsignadaRepository {

    /**
     * Guarda una nueva entrada asignada. Asume transacción externa activa.
     *
     * @param em EntityManager activo.
     * @param entradaAsignada La entidad a guardar (debe tener CompraEntrada
     * asociada).
     * @return La entidad guardada con su ID asignado.
     */
    EntradaAsignada save(EntityManager em, EntradaAsignada entradaAsignada);

    /**
     * Busca una entrada asignada por su ID.
     *
     * @param em EntityManager activo.
     * @param id El ID de la entrada asignada.
     * @return Optional con la entrada si se encuentra.
     */
    Optional<EntradaAsignada> findById(EntityManager em, Integer id);

    /**
     * Busca una entrada asignada por su código QR (que es único).
     *
     * @param em EntityManager activo.
     * @param codigoQr El código QR a buscar.
     * @return Optional con la entrada si se encuentra.
     */
    Optional<EntradaAsignada> findByCodigoQr(EntityManager em, String codigoQr);

    /**
     * Busca todas las entradas asignadas generadas a partir de un detalle de
     * compra.
     *
     * @param em EntityManager activo.
     * @param idCompraEntrada El ID del CompraEntrada.
     * @return Lista de entradas asignadas para ese detalle.
     */
    List<EntradaAsignada> findByCompraEntradaId(EntityManager em, Integer idCompraEntrada);

    /**
     * Busca todas las entradas asignadas para un festival específico.
     *
     * @param em EntityManager activo.
     * @param idFestival ID del festival.
     * @return Lista de entradas asignadas para ese festival.
     */
    List<EntradaAsignada> findByFestivalId(EntityManager em, Integer idFestival); // <-- NUEVO MÉTODO

}
