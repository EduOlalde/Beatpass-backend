package com.daw2edudiego.beatpasstfg.mapper;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.model.Compra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

// Añade la clase auxiliar al atributo 'uses'
@Mapper(componentModel = "jakarta", uses = {CompraHelper.class}) 
public interface CompraMapper {

    CompraMapper INSTANCE = Mappers.getMapper(CompraMapper.class);

    @Mapping(source = "comprador.idComprador", target = "idComprador")
    @Mapping(source = "comprador.nombre", target = "nombreComprador")
    @Mapping(source = "comprador.email", target = "emailComprador")
    @Mapping(target = "resumenEntradas", source = "detallesCompra", qualifiedByName = "mapResumenEntradas")
    @Mapping(target = "entradasGeneradas", ignore = true)
    CompraDTO compraToCompraDTO(Compra compra);

    List<CompraDTO> toCompraDTOList(List<Compra> compras);
}
