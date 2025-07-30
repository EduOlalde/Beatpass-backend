package com.beatpass.util;

import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.exception.UsuarioNotFoundException;
import com.beatpass.model.Festival;
import com.beatpass.model.RolUsuario;
import com.beatpass.model.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación del servicio de permisos centralizado. Contiene la lógica para
 * verificar si un usuario tiene autorización para realizar operaciones sobre
 * recursos específicos, como un festival.
 */
@ApplicationScoped
public class PermissionServiceImpl implements PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @PersistenceContext(unitName = "beatpassPersistenceUnit")
    private EntityManager em;

    @Override
    public void verificarPermisoSobreFestival(Integer idFestival, Integer idActor) {
        if (idFestival == null) {
            throw new FestivalNotFoundException("El ID del festival no puede ser nulo para la verificación de permisos.");
        }
        if (idActor == null) {
            throw new IllegalArgumentException("El ID del usuario actor no puede ser nulo.");
        }

        // Buscar al actor que realiza la acción
        Usuario actor = em.find(Usuario.class, idActor);
        if (actor == null) {
            throw new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor);
        }

        // Un ADMIN siempre tiene permiso
        if (actor.getRol() == RolUsuario.ADMIN) {
            log.trace("Permiso concedido para festival ID {} a usuario ID {} (Rol: ADMIN).", idFestival, idActor);
            return;
        }

        // Buscar el festival
        Festival festival = em.find(Festival.class, idFestival);
        if (festival == null) {
            throw new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival);
        }

        // Si el actor es PROMOTOR, verificar que sea el dueño
        if (actor.getRol() == RolUsuario.PROMOTOR) {
            if (festival.getPromotor() != null && festival.getPromotor().getIdUsuario().equals(idActor)) {
                log.trace("Permiso concedido para festival ID {} a usuario promotor dueño ID {}.", idFestival, idActor);
                return;
            }
        }

        // Un CAJERO tiene permiso si el festival está activo (PUBLICADO)
        if (actor.getRol() == RolUsuario.CAJERO) {
            if (festival.getEstado() == com.beatpass.model.EstadoFestival.PUBLICADO) {
                log.trace("Permiso concedido para festival ID {} a usuario ID {} (Rol: CAJERO en festival PUBLICADO).", idFestival, idActor);
                return;
            }
        }

        // Si no es ADMIN, el PROMOTOR dueño o un CAJERO en un festival publicado, denegar acceso.
        log.warn("Intento de acceso no autorizado por usuario ID {} (Rol: {}) al festival ID {} (Propiedad de Promotor ID {})",
                idActor,
                actor.getRol(),
                festival.getIdFestival(),
                festival.getPromotor() != null ? festival.getPromotor().getIdUsuario() : "N/A");
        throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
    }
}
