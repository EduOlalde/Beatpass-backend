package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
import java.io.IOException;
import java.util.List;

public interface PdfService {

    byte[] generarPdfMultiplesEntradas(List<EntradaAsignadaDTO> entradas, String nombreFestival) throws IOException;

    // Si quieres la opción de generar un PDF por cada entrada individualmente:
    // byte[] generarPdfParaEntrada(EntradaAsignadaDTO entrada) throws IOException;
}
