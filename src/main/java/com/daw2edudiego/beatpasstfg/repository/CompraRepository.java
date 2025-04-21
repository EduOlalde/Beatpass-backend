package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Compra;
import jakarta.persistence.EntityManager;
import java.util.Optional; // Importar Optional

/**
 * Interfaz DAO para la entidad Compra.
 * @author Eduardo Olalde
 */
public interface CompraRepository {

    /**
     * Guarda una nueva compra. Asume transacción externa activa.
     *
     * @param em EntityManager activo.
     * @param compra La entidad Compra a guardar (debe tener Asistente
     * asociado).
     * @return La entidad Compra guardada con su ID asignado.
     */
    Compra save(EntityManager em, Compra compra);

    /**
     * Busca una compra por su ID.
     *
     * @param em EntityManager activo.
     * @param id El ID de la compra.
     * @return Optional con la compra si se encuentra.
     */
    Optional<Compra> findById(EntityManager em, Integer id);

    // Podrían añadirse métodos como findByAsistenteId(em, idAsistente)
}
