package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import java.util.List;

/**
 * Interfaz que define la lógica de negocio y las operaciones relacionadas con
 * la gestión de Compras.
 *
 * @author Eduardo Olalde
 */
public interface CompraService {

    /**
     * Obtiene una lista de todas las compras realizadas para un festival
     * específico. Verifica que el festival exista y que el usuario (promotor)
     * que realiza la solicitud sea el propietario del festival.
     *
     * @param idFestival El ID del festival cuyas compras se quieren obtener.
     * @param idPromotor El ID del usuario (promotor) que realiza la consulta.
     * @return Una lista de {@link CompraDTO} que representan las compras del
     * festival.
     * @throws FestivalNotFoundException si el festival no se encuentra.
     * @throws UsuarioNotFoundException si el promotor no se encuentra.
     * @throws SecurityException si el promotor no tiene permisos sobre el
     * festival.
     * @throws IllegalArgumentException si idFestival o idPromotor son nulos.
     * @throws RuntimeException si ocurre un error inesperado durante la
     * consulta.
     */
    List<CompraDTO> obtenerComprasPorFestival(Integer idFestival, Integer idPromotor);

    // Aquí podrían añadirse otros métodos relacionados con compras si fueran necesarios
    // Ej: Optional<CompraDetalleDTO> obtenerDetalleCompra(Integer idCompra, Integer idUsuario);
}
