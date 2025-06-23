package com.beatpass.service;

import com.beatpass.dto.CompraDTO;
import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.exception.UsuarioNotFoundException;
import java.util.List;

/**
 * Define la lógica de negocio para la gestión de Compras.
 */
public interface CompraService {

    /**
     * Obtiene las compras realizadas para un festival específico. Verifica
     * permisos del promotor sobre el festival.
     *
     * @param idFestival ID del festival.
     * @param idPromotor ID del promotor solicitante.
     * @return Lista de CompraDTO.
     * @throws FestivalNotFoundException si el festival no se encuentra.
     * @throws UsuarioNotFoundException si el promotor no se encuentra.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    List<CompraDTO> obtenerComprasPorFestival(Integer idFestival, Integer idPromotor);

}
