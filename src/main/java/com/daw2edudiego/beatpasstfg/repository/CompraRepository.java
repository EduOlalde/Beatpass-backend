package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Compra;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para la entidad {@link Compra}. Define las
 * operaciones de persistencia para las cabeceras de compra.
 *
 * @author Eduardo Olalde
 */
public interface CompraRepository {

    /**
     * Guarda una nueva entidad Compra en la base de datos. Se asume que la
     * entidad Compra ya tiene asociado el
     * {@link com.daw2edudiego.beatpasstfg.model.Asistente} correspondiente. Los
     * detalles (CompraEntrada) normalmente se guardan por separado o mediante
     * cascada.
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param compra La entidad Compra a guardar. No debe ser nula, ni su
     * Asistente asociado.
     * @return La entidad Compra guardada, con su ID asignado por la base de
     * datos.
     * @throws IllegalArgumentException si compra o su asistente asociado son
     * nulos.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la persistencia.
     */
    Compra save(EntityManager em, Compra compra);

    /**
     * Busca una Compra por su identificador único (clave primaria).
     *
     * @param em El EntityManager activo.
     * @param id El ID de la compra a buscar.
     * @return Un {@link Optional} que contiene la Compra si se encuentra, o un
     * Optional vacío si no se encuentra o si el ID es nulo.
     */
    Optional<Compra> findById(EntityManager em, Integer id);

    /**
     * Busca y devuelve todas las compras realizadas por un asistente
     * específico.
     *
     * @param em El EntityManager activo.
     * @param idAsistente El ID del Asistente cuyas compras se quieren obtener.
     * @return Una lista (posiblemente vacía) de entidades Compra realizadas por
     * el asistente. Devuelve una lista vacía si el idAsistente es nulo o si
     * ocurre un error.
     */
    List<Compra> findByAsistenteId(EntityManager em, Integer idAsistente);

    /**
     * Busca y devuelve todas las compras asociadas a un festival específico. La
     * consulta navega a través de CompraEntrada y Entrada para filtrar por el
     * ID del festival. Se devuelven las compras únicas (DISTINCT).
     *
     * @param em El EntityManager activo.
     * @param idFestival El ID del festival cuyas compras se quieren obtener.
     * @return Una lista (posiblemente vacía) de entidades Compra únicas
     * asociadas al festival, ordenadas por fecha descendente. Devuelve lista
     * vacía si el idFestival es nulo o si ocurre un error.
     */
    List<Compra> findByFestivalId(EntityManager em, Integer idFestival); // NUEVO MÉTODO

    // Podrían añadirse otros métodos como:
    // List<Compra> findByFechaBetween(EntityManager em, LocalDateTime inicio, LocalDateTime fin);
    // void delete(EntityManager em, Compra compra);
}
