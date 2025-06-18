package com.daw2edudiego.beatpasstfg.mapper;

import com.daw2edudiego.beatpasstfg.dto.AsistenteDTO;
import com.daw2edudiego.beatpasstfg.model.Asistente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "jakarta")
public interface AsistenteMapper {

    AsistenteMapper INSTANCE = Mappers.getMapper(AsistenteMapper.class);

    @Mapping(target = "fechaCreacion", expression = "java(asistente.getFechaCreacion() != null ? asistente.getFechaCreacion() : null)")
    @Mapping(target = "festivalPulseraInfo", ignore = true) // Este campo se llenará manualmente en el servicio
    AsistenteDTO asistenteToAsistenteDTO(Asistente asistente);

    @Mapping(target = "idAsistente", ignore = true) // ID es generado por la BD
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "entradas", ignore = true) // La colección se gestiona por JPA
    Asistente asistenteDTOToAsistente(AsistenteDTO asistenteDTO);

    List<AsistenteDTO> toAsistenteDTOList(List<Asistente> asistentes);

    // Método para actualizar una entidad existente desde un DTO
    @Mapping(target = "idAsistente", ignore = true)
    @Mapping(target = "email", ignore = true) // Email no es actualizable en el DTO Asistente, se mantiene el de la entidad
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "entradas", ignore = true)
    void updateAsistenteFromDto(AsistenteDTO asistenteDTO, @MappingTarget Asistente asistente);
}
