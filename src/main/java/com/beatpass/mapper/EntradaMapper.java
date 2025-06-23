package com.beatpass.mapper;

import com.beatpass.dto.EntradaDTO;
import com.beatpass.model.Entrada;
import com.beatpass.util.QRCodeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Mapper(componentModel = "jakarta")
public interface EntradaMapper {

    EntradaMapper INSTANCE = Mappers.getMapper(EntradaMapper.class);

    @Mapping(source = "compraEntrada.idCompraEntrada", target = "idCompraEntrada")
    @Mapping(source = "compraEntrada.tipoEntrada.idTipoEntrada", target = "idEntradaOriginal")
    @Mapping(source = "compraEntrada.tipoEntrada.tipo", target = "tipoEntradaOriginal")
    @Mapping(source = "compraEntrada.tipoEntrada.requiereNominacion", target = "requiereNominacion")
    @Mapping(source = "compraEntrada.tipoEntrada.festival.idFestival", target = "idFestival")
    @Mapping(source = "compraEntrada.tipoEntrada.festival.nombre", target = "nombreFestival")
    @Mapping(source = "asistente.idAsistente", target = "idAsistente")
    @Mapping(source = "asistente.nombre", target = "nombreAsistente")
    @Mapping(source = "asistente.email", target = "emailAsistente")
    @Mapping(source = "pulseraAsociada.idPulsera", target = "idPulseraAsociada")
    @Mapping(source = "pulseraAsociada.codigoUid", target = "codigoUidPulsera")
    @Mapping(target = "fechaAsignacion", source = "fechaAsignacion", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(target = "fechaUso", source = "fechaUso", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(target = "qrCodeImageDataUrl", source = "codigoQr", qualifiedByName = "generateQrImage")
    EntradaDTO entradaToEntradaDTO(Entrada entrada);

    List<EntradaDTO> toEntradaDTOList(List<Entrada> entradas);

    @Named("generateQrImage")
    default String generateQrImage(String codigoQr) {
        if (codigoQr != null && !codigoQr.isBlank()) {
            return QRCodeUtil.generarQrComoBase64(codigoQr, 100, 100);
        }
        return null;
    }

    default Date toDate(java.time.LocalDateTime localDateTime) {
        return localDateTime != null ? Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    default java.time.LocalDateTime toLocalDateTime(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }
}
