package com.beatpass.mapper;

import com.beatpass.model.CompraEntrada;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Clase auxiliar para el mapeador de Compras (CompraMapper). Contiene lógica de
 * mapeo personalizada que no puede ser generada automáticamente por MapStruct.
 */
public class CompraHelper {

    /**
     * Mapea un conjunto de entidades CompraEntrada a una lista de cadenas que
     * resumen las entradas compradas (ej. "2 x General").
     *
     * @param detallesCompra El conjunto de detalles de la compra.
     * @return Una lista de cadenas con el resumen, o null si la entrada es
     * nula.
     */
    @Named("mapResumenEntradas")
    public static List<String> mapResumenEntradas(Set<CompraEntrada> detallesCompra) {
        if (detallesCompra == null) {
            return null;
        }
        return detallesCompra.stream()
                .map(detalle -> detalle.getCantidad() + " x " + (detalle.getTipoEntrada() != null ? detalle.getTipoEntrada().getTipo() : "Desconocido"))
                .collect(Collectors.toList());
    }
}
