package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.Compra;
import com.daw2edudiego.beatpasstfg.model.CompraEntrada;
import com.daw2edudiego.beatpasstfg.model.Comprador;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import com.daw2edudiego.beatpasstfg.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de CompraService.
 */
public class CompraServiceImpl extends AbstractService implements CompraService {

    private static final Logger log = LoggerFactory.getLogger(CompraServiceImpl.class);

    private final CompraRepository compraRepository;
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;

    public CompraServiceImpl() {
        this.compraRepository = new CompraRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    @Override
    public List<CompraDTO> obtenerComprasPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo compras para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival e ID de promotor son requeridos.");
        }

        return executeRead(em -> {
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor);

            List<Compra> compras = compraRepository.findByFestivalId(em, idFestival);
            log.info("Encontradas {} compras para el festival ID {} (Promotor {})", compras.size(), idFestival, idPromotor);
            return compras.stream()
                    .map(this::mapCompraToDto)
                    .collect(Collectors.toList());
        }, "obtenerComprasPorFestival " + idFestival);
    }

    // --- Métodos Privados de Ayuda ---
    /**
     * Mapea entidad Compra a DTO.
     */
    private CompraDTO mapCompraToDto(Compra compra) {
        if (compra == null) {
            return null;
        }
        CompraDTO dto = new CompraDTO();
        dto.setIdCompra(compra.getIdCompra());
        dto.setFechaCompra(compra.getFechaCompra());
        dto.setTotal(compra.getTotal());
        dto.setStripePaymentIntentId(compra.getStripePaymentIntentId());
        dto.setEstadoPago(compra.getEstadoPago());
        dto.setFechaPagoConfirmado(compra.getFechaPagoConfirmado());

        Comprador comprador = compra.getComprador();
        if (comprador != null) {
            dto.setIdComprador(comprador.getIdComprador());
            dto.setNombreComprador(comprador.getNombre());
            dto.setEmailComprador(comprador.getEmail());
        }

        List<String> resumen = new ArrayList<>();
        if (compra.getDetallesCompra() != null) {
            for (CompraEntrada detalle : compra.getDetallesCompra()) {
                String tipo = (detalle.getTipoEntrada() != null) ? detalle.getTipoEntrada().getTipo() : "Desconocido";
                resumen.add(detalle.getCantidad() + " x " + tipo);
            }
        }
        dto.setResumenEntradas(resumen);
        // El campo entradasGeneradas no se popula aquí, solo en la confirmación de venta.
        return dto;
    }

    /**
     * Verifica que el promotor sea propietario del festival.
     */
    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null) {
            throw new FestivalNotFoundException("El festival asociado no puede ser nulo.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor no puede ser nulo.");
        }
        if (festival.getPromotor() == null || !festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            log.warn("Intento de acceso no autorizado por promotor ID {} al festival ID {}",
                    idPromotor, festival.getIdFestival());
            throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
        }
    }
}
