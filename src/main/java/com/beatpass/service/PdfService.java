package com.beatpass.service;

import com.beatpass.dto.EntradaDTO;
import java.io.IOException;
import java.util.List;

public interface PdfService {

    byte[] generarPdfMultiplesEntradas(List<EntradaDTO> entradas, String nombreFestival) throws IOException;

    // Si quieres la opción de generar un PDF por cada entrada individualmente:
    // byte[] generarPdfParaEntrada(EntradaDTO entrada) throws IOException;
}
