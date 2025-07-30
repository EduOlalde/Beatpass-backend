package com.beatpass.service;

import com.beatpass.dto.CompraDTO;
import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.mapper.CompraMapper;
import com.beatpass.model.Compra;
import com.beatpass.repository.CompraRepository;
import com.beatpass.util.PermissionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class CompraServiceImpl implements CompraService {

    private static final Logger log = LoggerFactory.getLogger(CompraServiceImpl.class);

    @Inject
    private CompraRepository compraRepository;

    @Inject
    private CompraMapper compraMapper;

    @Inject
    private PermissionService permissionService;

    @Override
    public List<CompraDTO> obtenerComprasPorFestival(Integer idFestival, Integer idActor) {
        log.debug("Service: Obteniendo compras para festival ID {} por actor ID {}", idFestival, idActor);
        if (idFestival == null || idActor == null) {
            throw new IllegalArgumentException("ID de festival e ID de actor son requeridos.");
        }

        // Delegamos la verificación de permisos al servicio especializado.
        // Si el actor no tiene permiso, este método lanzará una excepción.
        permissionService.verificarPermisoSobreFestival(idFestival, idActor);

        List<Compra> compras = compraRepository.findByFestivalId(idFestival);
        log.info("Encontradas {} compras para el festival ID {} (Actor {})", compras.size(), idFestival, idActor);
        return compraMapper.toCompraDTOList(compras);
    }
}
