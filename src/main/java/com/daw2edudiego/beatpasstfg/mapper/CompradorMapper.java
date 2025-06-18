package com.daw2edudiego.beatpasstfg.mapper;

import com.daw2edudiego.beatpasstfg.dto.CompradorDTO;
import com.daw2edudiego.beatpasstfg.model.Comprador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "jakarta")
public interface CompradorMapper {

    CompradorMapper INSTANCE = Mappers.getMapper(CompradorMapper.class);

    CompradorDTO compradorToCompradorDTO(Comprador comprador);

    @Mapping(target = "idComprador", ignore = true) // ID es generado por la BD
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "compras", ignore = true) // La colección se gestiona por JPA
    Comprador compradorDTOToComprador(CompradorDTO compradorDTO);

    List<CompradorDTO> toCompradorDTOList(List<Comprador> compradores);
}
