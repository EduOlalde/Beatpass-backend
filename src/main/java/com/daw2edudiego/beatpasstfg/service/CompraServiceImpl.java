package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.Compra;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario; // Import RolUsuario
import com.daw2edudiego.beatpasstfg.model.Usuario; // Import Usuario
import com.daw2edudiego.beatpasstfg.repository.*;
import jakarta.persistence.EntityManager; // Import EntityManager
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daw2edudiego.beatpasstfg.mapper.CompraMapper;

import java.util.List;

/**
 * Implementación de CompraService.
 */
public class CompraServiceImpl extends AbstractService implements CompraService {

    private static final Logger log = LoggerFactory.getLogger(CompraServiceImpl.class);

    private final CompraRepository compraRepository;
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository; // Added for fetching User for role check
    private final CompraMapper compraMapper;

    public CompraServiceImpl() {
        this.compraRepository = new CompraRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl(); // Initialize
        this.compraMapper = CompraMapper.INSTANCE;
    }

    @Override
    public List<CompraDTO> obtenerComprasPorFestival(Integer idFestival, Integer idActor) { // Renamed idPromotor to idActor for clarity
        log.debug("Service: Obteniendo compras para festival ID {} por actor ID {}", idFestival, idActor);
        if (idFestival == null || idActor == null) {
            throw new IllegalArgumentException("ID de festival e ID de actor son requeridos.");
        }

        return executeRead(em -> {
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(em, festival, idActor); // Pass em

            List<Compra> compras = compraRepository.findByFestivalId(em, idFestival);
            log.info("Encontradas {} compras para el festival ID {} (Actor {})", compras.size(), idFestival, idActor);
            return compraMapper.toCompraDTOList(compras);
        }, "obtenerComprasPorFestival " + idFestival);
    }

    // --- Métodos Privados de Ayuda ---
    /**
     * Verifica que el usuario actor (ADMIN o PROMOTOR) tenga permiso sobre el
     * festival. Si es PROMOTOR, debe ser el propietario del festival. Si es
     * ADMIN, siempre tiene acceso.
     */
    private void verificarPropiedadFestival(EntityManager em, Festival festival, Integer idActor) { // Added EntityManager parameter
        if (festival == null) {
            throw new FestivalNotFoundException("El festival asociado no puede ser nulo.");
        }
        if (idActor == null) {
            throw new IllegalArgumentException("El ID del usuario actor no puede ser nulo.");
        }

        // Fetch the acting user (actor) to check their role.
        Usuario actor = em.find(Usuario.class, idActor); // Use em.find directly here for simplicity and direct management

        if (actor == null) {
            throw new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor);
        }

        boolean isActorAdmin = (actor.getRol() != null && actor.getRol() == RolUsuario.ADMIN); // Added null check for safety
        boolean isActorPromotorOwner = (festival.getPromotor() != null && festival.getPromotor().getIdUsuario().equals(idActor));

        if (!(isActorAdmin || isActorPromotorOwner)) {
            log.warn("Intento de acceso no autorizado por usuario ID {} (Rol: {}) al festival ID {} (Prop. por Promotor ID {})",
                    idActor, (actor.getRol() != null ? actor.getRol() : "NULL_ROL"), festival.getIdFestival(), festival.getPromotor() != null ? festival.getPromotor().getIdUsuario() : "N/A");
            throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
        }
    }
}
