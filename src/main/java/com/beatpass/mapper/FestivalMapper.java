package com.beatpass.mapper;

import com.beatpass.dto.FestivalDTO;
import com.beatpass.model.Festival;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "jakarta")
public interface FestivalMapper {

    FestivalMapper INSTANCE = Mappers.getMapper(FestivalMapper.class);

    @Mapping(source = "promotor.idUsuario", target = "idPromotor")
    @Mapping(source = "promotor.nombre", target = "nombrePromotor")
    FestivalDTO festivalToFestivalDTO(Festival festival);

    @Mapping(target = "idFestival", ignore = true) // ID es generado por la BD
    @Mapping(target = "promotor", ignore = true) // El promotor se setea en el servicio
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "tiposEntrada", ignore = true) // Colecciones gestionadas por JPA
    @Mapping(target = "consumos", ignore = true)
    @Mapping(target = "estadisticas", ignore = true)
    Festival festivalDTOToFestival(FestivalDTO festivalDTO);

    List<FestivalDTO> toFestivalDTOList(List<Festival> festivals);

    // Método para actualizar una entidad existente desde un DTO
    @Mapping(target = "idFestival", ignore = true)
    @Mapping(target = "promotor", ignore = true)
    @Mapping(target = "estado", ignore = true) // El estado se actualiza por un método específico
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "tiposEntrada", ignore = true)
    @Mapping(target = "consumos", ignore = true)
    @Mapping(target = "estadisticas", ignore = true)
    void updateFestivalFromDto(FestivalDTO festivalDTO, @MappingTarget Festival festival);
}
