package com.beatpass.repository;

import com.beatpass.model.EstadoFestival;
import com.beatpass.model.Festival;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Festival.
 */
public interface FestivalRepository {

    /**
     * Guarda (crea o actualiza) un Festival. Debe ejecutarse dentro de una
     * transacción activa.
     *
     * @param festival El festival a guardar.
     * @return La entidad Festival guardada o actualizada.
     */
    Festival save(Festival festival);

    /**
     * Busca un Festival por su ID.
     *
     * @param id El ID a buscar.
     * @return Un Optional con el Festival si se encuentra, o vacío.
     */
    Optional<Festival> findById(Integer id);

    /**
     * Elimina un festival por su ID. Debe ejecutarse dentro de una transacción
     * activa. Considerar cascada.
     *
     * @param id El ID a eliminar.
     * @return true si se encontró y marcó para eliminar, false si no.
     */
    boolean deleteById(Integer id);

    /**
     * Busca todos los festivales. Usar con precaución.
     *
     * @return Una lista con todos los festivales.
     */
    List<Festival> findAll();

    /**
     * Busca todos los festivales en un estado específico.
     *
     * @param estado El EstadoFestival a buscar.
     * @return Una lista (posiblemente vacía) de festivales.
     */
    List<Festival> findByEstado(EstadoFestival estado);

    /**
     * Busca festivales activos (PUBLICADO) cuyo periodo se solapa con las
     * fechas dadas.
     *
     * @param fechaDesde Fecha de inicio del rango.
     * @param fechaHasta Fecha de fin del rango.
     * @return Una lista (posiblemente vacía) de festivales.
     */
    List<Festival> findActivosEntreFechas(LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Busca todos los festivales gestionados por un Promotor específico.
     *
     * @param idPromotor El ID del Promotor.
     * @return Una lista (posiblemente vacía) de festivales.
     */
    List<Festival> findByPromotorId(Integer idPromotor);

}
