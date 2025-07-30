package com.beatpass.util;

public interface PermissionService {
    
    /**
     * Método centralizado de autorización. Verifica si un usuario (actor) tiene
     * permisos sobre un festival. El permiso se concede si el actor es ADMIN o
     * si es un PROMOTOR dueño del festival.
     *
     * @param idFestival El ID del festival sobre el cual se verifica el
     * permiso.
     * @param idActor El ID del usuario que intenta realizar la acción.
     * @throws UsuarioNotFoundException Si el actor no se encuentra.
     * @throws FestivalNotFoundException Si el festival no se encuentra.
     * @throws SecurityException Si el actor no tiene los permisos requeridos.
     * @throws IllegalArgumentException Si alguno de los IDs es nulo.
     */
    void verificarPermisoSobreFestival(Integer idFestival, Integer idActor);
}
