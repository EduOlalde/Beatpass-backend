package com.daw2edudiego.beatpasstfg.mapper;

import com.daw2edudiego.beatpasstfg.model.CompraEntrada;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set; 
import java.util.stream.Collectors;

public class CompraHelper {

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
