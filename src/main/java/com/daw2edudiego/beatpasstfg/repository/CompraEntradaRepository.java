package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.CompraEntrada;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para la entidad {@link CompraEntrada}, que
 * representa una línea de detalle dentro de una compra. Define las operaciones
 * de persistencia para estos detalles.
 *
 * @author Eduardo Olalde
 */
public interface CompraEntradaRepository {

    /**
     * Guarda una nueva entidad CompraEntrada en la base de datos. Se asume que
     * la entidad CompraEntrada ya tiene asociadas las entidades
     * {@link com.daw2edudiego.beatpasstfg.model.Compra} y
     * {@link com.daw2edudiego.beatpasstfg.model.Entrada} correspondientes.
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param compraEntrada El detalle de compra a guardar. No debe ser nulo, ni
     * sus relaciones Compra y Entrada.
     * @return El detalle de compra guardado, con su ID asignado por la base de
     * datos.
     * @throws IllegalArgumentException si compraEntrada o sus relaciones clave
     * son nulas.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la persistencia.
     */
    CompraEntrada save(EntityManager em, CompraEntrada compraEntrada);

    /**
     * Busca un detalle de compra (CompraEntrada) por su identificador único
     * (clave primaria).
     *
     * @param em El EntityManager activo.
     * @param id El ID del detalle de compra a buscar.
     * @return Un {@link Optional} que contiene el CompraEntrada si se
     * encuentra, o un Optional vacío si no se encuentra o si el ID es nulo.
     */
    Optional<CompraEntrada> findById(EntityManager em, Integer id);

    /**
     * Busca y devuelve todos los detalles de compra asociados a una
     * {@link com.daw2edudiego.beatpasstfg.model.Compra} específica.
     *
     * @param em El EntityManager activo.
     * @param idCompra El ID de la Compra cuyos detalles se quieren obtener.
     * @return Una lista (posiblemente vacía) de entidades CompraEntrada
     * asociadas a la compra dada. Devuelve una lista vacía si el idCompra es
     * nulo o si ocurre un error.
     */
    List<CompraEntrada> findByCompraId(EntityManager em, Integer idCompra);

    // Se podrían añadir otros métodos si fueran necesarios, por ejemplo:
    // List<CompraEntrada> findByEntradaId(EntityManager em, Integer idEntrada);
    // void delete(EntityManager em, CompraEntrada compraEntrada);
}
