package com.beatpass.service;

import com.beatpass.repository.CompraRepository;
import com.beatpass.repository.UsuarioRepository;
import com.beatpass.repository.FestivalRepository;
import com.beatpass.dto.CompraDTO;
import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.exception.UsuarioNotFoundException;
import com.beatpass.model.Compra;
import com.beatpass.model.Festival;
import com.beatpass.model.RolUsuario;
import com.beatpass.model.Usuario;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beatpass.mapper.CompraMapper;

import java.util.List;

/**
 * Implementación de CompraService.
 */
public class CompraServiceImpl extends AbstractService implements CompraService {

    private static final Logger log = LoggerFactory.getLogger(CompraServiceImpl.class);

    private final CompraRepository compraRepository;
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompraMapper compraMapper;

    @Inject
    public CompraServiceImpl(CompraRepository compraRepository, FestivalRepository festivalRepository, UsuarioRepository usuarioRepository) {
        this.compraRepository = compraRepository;
        this.festivalRepository = festivalRepository;
        this.usuarioRepository = usuarioRepository;
        this.compraMapper = CompraMapper.INSTANCE;
    }

    @Override
    public List<CompraDTO> obtenerComprasPorFestival(Integer idFestival, Integer idActor) {
        log.debug("Service: Obteniendo compras para festival ID {} por actor ID {}", idFestival, idActor);
        if (idFestival == null || idActor == null) {
            throw new IllegalArgumentException("ID de festival e ID de actor son requeridos.");
        }

        return executeRead(em -> {
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPermisoSobreFestival(em, festival.getIdFestival(), idActor);

            List<Compra> compras = compraRepository.findByFestivalId(em, idFestival);
            log.info("Encontradas {} compras para el festival ID {} (Actor {})", compras.size(), idFestival, idActor);
            return compraMapper.toCompraDTOList(compras);
        }, "obtenerComprasPorFestival " + idFestival);
    }
}
