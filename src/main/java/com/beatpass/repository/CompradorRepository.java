package com.beatpass.repository;

import com.beatpass.model.Comprador;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Comprador.
 */
public interface CompradorRepository {

    /**
     * Guarda (crea o actualiza) una entidad Comprador. Debe ejecutarse dentro
     * de una transacción activa.
     *
     * @param comprador La entidad Comprador a guardar.
     * @return La entidad Comprador guardada o actualizada.
     */
    Comprador save(Comprador comprador);

    /**
     * Busca un Comprador por su ID.
     *
     * @param id El ID del comprador a buscar.
     * @return Un Optional con el Comprador si se encuentra, o vacío.
     */
    Optional<Comprador> findById(Integer id);

    /**
     * Busca un Comprador por su email (que es único).
     *
     * @param email El email a buscar.
     * @return Un Optional con el Comprador si se encuentra, o vacío.
     */
    Optional<Comprador> findByEmail(String email);

    /**
     * Busca compradores cuyo nombre o email coincidan con el término de
     * búsqueda. Si el término es nulo o vacío, devuelve todos los compradores.
     *
     * @param searchTerm El término a buscar.
     * @return Una lista de compradores que coinciden.
     */
    List<Comprador> searchByTerm(String searchTerm);
}
