package com.daw2edudiego.beatpasstfg.mapper;

import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "jakarta")
public interface UsuarioMapper {

    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);

    UsuarioDTO usuarioToUsuarioDTO(Usuario usuario);

    @Mapping(target = "idUsuario", ignore = true) // ID es generado por la BD
    @Mapping(target = "password", ignore = true) // La contraseña se hashea y setea en el servicio
    @Mapping(target = "estado", ignore = true) // El estado por defecto se setea en la entidad/servicio
    @Mapping(target = "cambioPasswordRequerido", ignore = true) // Se setea en el servicio
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "festivales", ignore = true) // Las colecciones se gestionan por JPA
    @Mapping(target = "recargasRealizadas", ignore = true)
    Usuario usuarioCreacionDTOToUsuario(UsuarioCreacionDTO usuarioCreacionDTO);

    List<UsuarioDTO> toUsuarioDTOList(List<Usuario> usuarios);

    // Método para actualizar una entidad Usuario existente desde un UsuarioDTO
    // Útil para cuando solo se quiere actualizar ciertos campos como el nombre o estado
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "cambioPasswordRequerido", ignore = true) 
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "festivales", ignore = true)
    @Mapping(target = "recargasRealizadas", ignore = true)
    void updateUsuarioFromDto(UsuarioDTO usuarioDTO, @MappingTarget Usuario usuario);

}
