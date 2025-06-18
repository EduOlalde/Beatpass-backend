package com.daw2edudiego.beatpasstfg.mapper;

import com.daw2edudiego.beatpasstfg.dto.PulseraNFCDTO;
import com.daw2edudiego.beatpasstfg.model.PulseraNFC;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "jakarta")
public interface PulseraNFCMapper {

    PulseraNFCMapper INSTANCE = Mappers.getMapper(PulseraNFCMapper.class);

    @Mapping(source = "entrada.idEntrada", target = "idEntrada")
    @Mapping(source = "entrada.codigoQr", target = "qrEntrada")
    @Mapping(source = "entrada.asistente.idAsistente", target = "idAsistente")
    @Mapping(source = "entrada.asistente.nombre", target = "nombreAsistente")
    @Mapping(source = "entrada.asistente.email", target = "emailAsistente")
    @Mapping(source = "festival.idFestival", target = "idFestival")
    @Mapping(source = "festival.nombre", target = "nombreFestival")
    PulseraNFCDTO pulseraNFCToPulseraNFCDTO(PulseraNFC pulseraNFC);

    @Mapping(target = "idPulsera", ignore = true) // ID es generado por la BD
    @Mapping(target = "fechaAlta", ignore = true)
    @Mapping(target = "ultimaModificacion", ignore = true)
    @Mapping(target = "fechaAsociacion", ignore = true)
    @Mapping(target = "entrada", ignore = true) // Se setea manualmente en el servicio
    @Mapping(target = "festival", ignore = true) // Se setea manualmente en el servicio
    @Mapping(target = "recargas", ignore = true) // Colecciones gestionadas por JPA
    @Mapping(target = "consumos", ignore = true)
    PulseraNFC pulseraNFCDTOToPulseraNFC(PulseraNFCDTO pulseraNFCDTO);

    List<PulseraNFCDTO> toPulseraNFCDTOList(List<PulseraNFC> pulserasNFC);

    // Método para actualizar una entidad existente desde un DTO (uso limitado, ya que el DTO es de lectura/respuesta)
    @Mapping(target = "idPulsera", ignore = true)
    @Mapping(target = "codigoUid", ignore = true)
    @Mapping(target = "fechaAlta", ignore = true)
    @Mapping(target = "ultimaModificacion", ignore = true)
    @Mapping(target = "fechaAsociacion", ignore = true)
    @Mapping(target = "entrada", ignore = true)
    @Mapping(target = "festival", ignore = true)
    @Mapping(target = "recargas", ignore = true)
    @Mapping(target = "consumos", ignore = true)
    void updatePulseraNFCFromDto(PulseraNFCDTO pulseraNFCDTO, @MappingTarget PulseraNFC pulseraNFC);
}
