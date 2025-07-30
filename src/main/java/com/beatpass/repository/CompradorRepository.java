package com.beatpass.repository;

import com.beatpass.model.Comprador;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Comprador.
 */
public interface CompradorRepository {

    Comprador save(Comprador comprador);

    Optional<Comprador> findById(Integer id);

    Optional<Comprador> findByEmail(String email);

    /**
     * Busca compradores cuyo nombre o email coincidan con el término de
     * búsqueda.
     *
     * @param searchTerm El término a buscar.
     * @return Una lista de compradores que coinciden.
     */
    List<Comprador> searchByTerm(String searchTerm);
}
