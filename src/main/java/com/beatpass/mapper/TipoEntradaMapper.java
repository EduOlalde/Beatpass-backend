package com.beatpass.mapper;

import com.beatpass.dto.TipoEntradaDTO;
import com.beatpass.model.TipoEntrada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "jakarta")
public interface TipoEntradaMapper {

    TipoEntradaMapper INSTANCE = Mappers.getMapper(TipoEntradaMapper.class);

    @Mapping(source = "festival.idFestival", target = "idFestival")
    TipoEntradaDTO tipoEntradaToTipoEntradaDTO(TipoEntrada tipoEntrada);

    @Mapping(target = "idTipoEntrada", ignore = true) // ID es generado por la BD
    @Mapping(target = "festival", ignore = true) // El festival se setea en el servicio
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "comprasDondeAparece", ignore = true) // La colección se gestiona por JPA
    TipoEntrada tipoEntradaDTOToTipoEntrada(TipoEntradaDTO tipoEntradaDTO);

    List<TipoEntradaDTO> toTipoEntradaDTOList(List<TipoEntrada> tiposEntrada);

    // Método para actualizar una entidad existente desde un DTO
    @Mapping(target = "idTipoEntrada", ignore = true)
    @Mapping(target = "festival", ignore = true) // <-- Corrección aquí: Ignorar la relación "festival"
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "comprasDondeAparece", ignore = true)
    void updateTipoEntradaFromDto(TipoEntradaDTO tipoEntradaDTO, @MappingTarget TipoEntrada tipoEntrada);
}
