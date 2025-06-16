package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.model.Comprador;

/**
 * Define la lógica de negocio para la gestión de Compradores.
 */
public interface CompradorService {

    /**
     * Busca un comprador por email. Si no existe, lo crea. Es transaccional si
     * implica creación.
     *
     * @param email Email del comprador (obligatorio).
     * @param nombre Nombre (obligatorio si se crea).
     * @param telefono Teléfono (opcional).
     * @return La entidad Comprador (existente o nueva).
     * @throws IllegalArgumentException Si faltan datos obligatorios para crear.
     */
    Comprador obtenerOcrearCompradorPorEmail(String email, String nombre, String telefono);
}
