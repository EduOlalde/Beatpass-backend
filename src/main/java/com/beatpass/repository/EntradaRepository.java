package com.beatpass.repository;

import com.beatpass.model.Entrada;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Entrada (entrada individual).
 */
public interface EntradaRepository {

    /**
     * Guarda (crea o actualiza) una Entrada. Debe ejecutarse dentro de
     * una transacción activa.
     *
     * @param entrada La entidad a guardar.
     * @return La entidad guardada o actualizada.
     */
    Entrada save(Entrada entrada);

    /**
     * Busca una Entrada por su ID.
     *
     * @param id El ID a buscar.
     * @return Un Optional con la Entrada si se encuentra, o vacío.
     */
    Optional<Entrada> findById(Integer id);

    /**
     * Busca una Entrada por su código QR (único).
     *
     * @param codigoQr El código QR a buscar.
     * @return Un Optional con la Entrada si se encuentra, o vacío.
     */
    Optional<Entrada> findByCodigoQr(String codigoQr);

    /**
     * Busca todas las entradas generadas desde un CompraEntrada
     * específico.
     *
     * @param idCompraEntrada El ID del CompraEntrada origen.
     * @return Una lista (posiblemente vacía) de Entrada.
     */
    List<Entrada> findByCompraEntradaId(Integer idCompraEntrada);

    /**
     * Busca todas las entradas que pertenecen a un Festival
     * específico.
     *
     * @param idFestival El ID del Festival.
     * @return Una lista (posiblemente vacía) de Entrada.
     */
    List<Entrada> findByFestivalId(Integer idFestival);

}
