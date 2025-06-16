package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
// QRCodeUtil no es necesario si el DTO ya tiene la imagen base64
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class PdfServiceImpl implements PdfService {

    private static final Logger log = LoggerFactory.getLogger(PdfServiceImpl.class);

    // Implementación de generar un PDF con múltiples entradas, una por página.
    @Override
    public byte[] generarPdfMultiplesEntradas(List<EntradaDTO> entradas, String nombreFestival) throws IOException {
        if (entradas == null || entradas.isEmpty()) {
            log.warn("No se pueden generar entradas en PDF: la lista de entradas está vacía o es nula.");
            return new byte[0]; // Devuelve un array vacío si no hay entradas
        }

        try (PDDocument document = new PDDocument()) {
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float lineHeight = 15f; // Espacio entre líneas de texto
            float margin = 40f;   // Margen de la página

            for (EntradaDTO entrada : entradas) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                    float currentY = PDRectangle.A4.getHeight() - margin;

                    // Logo (Placeholder - Necesitarías tu imagen de logo)
                    // try {
                    //     InputStream logoStream = getClass().getResourceAsStream("/path/to/your/logo.png");
                    //     if (logoStream != null) {
                    //          PDImageXObject pdLogo = PDImageXObject.createFromByteArray(document, logoStream.readAllBytes(), "logo");
                    //          float logoWidth = 100;
                    //          float logoHeight = (pdLogo.getHeight() * logoWidth) / pdLogo.getWidth(); // Mantener proporción
                    //          contentStream.drawImage(pdLogo, margin, currentY - logoHeight, logoWidth, logoHeight);
                    //          currentY -= (logoHeight + lineHeight);
                    //     }
                    // } catch (Exception e_logo) {
                    //     log.warn("No se pudo cargar el logo para el PDF: {}", e_logo.getMessage());
                    // }
                    // Título del Festival
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 18);
                    contentStream.newLineAtOffset(margin, currentY);
                    contentStream.showText("Entrada para: " + (nombreFestival != null ? nombreFestival : "Festival Beatpass"));
                    contentStream.endText();
                    currentY -= (lineHeight * 2);

                    // Detalles de la Entrada
                    contentStream.beginText();
                    contentStream.setFont(fontRegular, 12);
                    contentStream.newLineAtOffset(margin, currentY);
                    contentStream.showText("Tipo de Entrada: ");
                    contentStream.setFont(fontBold, 12);
                    contentStream.showText(entrada.getTipoEntradaOriginal() != null ? entrada.getTipoEntradaOriginal() : "N/A");
                    contentStream.setFont(fontRegular, 12);
                    currentY -= lineHeight;
                    contentStream.newLineAtOffset(0, -lineHeight);

                    contentStream.showText("Asistente: ");
                    contentStream.setFont(fontBold, 12);
                    contentStream.showText(entrada.getNombreAsistente() != null ? entrada.getNombreAsistente() : "Pendiente de nominar");
                    contentStream.setFont(fontRegular, 12);
                    currentY -= lineHeight;
                    contentStream.newLineAtOffset(0, -lineHeight);

                    /* Si se quiere incluir el email
                    contentStream.showText("Email: ");
                    contentStream.setFont(fontBold, 12);
                    contentStream.showText(entrada.getEmailAsistente() != null ? entrada.getEmailAsistente() : "N/A");
                    contentStream.setFont(fontRegular, 12);
                    currentY -= lineHeight;
                    contentStream.newLineAtOffset(0, -lineHeight);
                    */

                    contentStream.showText("Código QR: ");
                    contentStream.setFont(fontBold, 12);
                    contentStream.showText(entrada.getCodigoQr() != null ? entrada.getCodigoQr() : "N/A");
                    contentStream.setFont(fontRegular, 12);
                    contentStream.endText();

                    currentY -= (lineHeight * 1.5f); // Más espacio antes del QR

                    // Imagen del Código QR
                    if (entrada.getQrCodeImageDataUrl() != null && entrada.getQrCodeImageDataUrl().startsWith("data:image/png;base64,")) {
                        try {
                            String base64Image = entrada.getQrCodeImageDataUrl().substring("data:image/png;base64,".length());
                            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, "qr-" + entrada.getIdEntrada());

                            float qrSize = 140f; // Tamaño del QR en el PDF
                            float qrX = (page.getMediaBox().getWidth() - qrSize) / 2; // Centrar QR horizontalmente
                            float qrY = currentY - qrSize - lineHeight; // Posicionar debajo del texto

                            contentStream.drawImage(pdImage, qrX, qrY, qrSize, qrSize);
                            currentY = qrY - (lineHeight * 1.5f); // Actualizar Y después del QR
                            log.debug("Imagen QR añadida al PDF para la entrada ID {}", entrada.getIdEntrada());
                        } catch (IllegalArgumentException | IOException e_qr) {
                            log.error("Error al decodificar o añadir la imagen QR Base64 al PDF para entrada ID {}: {}", entrada.getIdEntrada(), e_qr.getMessage());
                            contentStream.beginText();
                            contentStream.setFont(fontRegular, 10);
                            contentStream.newLineAtOffset(margin, currentY - lineHeight);
                            contentStream.showText("Error al mostrar QR. Presentar código: " + (entrada.getCodigoQr() != null ? entrada.getCodigoQr() : "N/A"));
                            contentStream.endText();
                            currentY -= (lineHeight * 2);
                        }
                    } else {
                        log.warn("No se encontró URL de imagen QR válida para la entrada ID {}", entrada.getIdEntrada());
                        contentStream.beginText();
                        contentStream.setFont(fontRegular, 10);
                        contentStream.newLineAtOffset(margin, currentY - lineHeight);
                        contentStream.showText("QR no disponible visualmente. Presentar código: " + (entrada.getCodigoQr() != null ? entrada.getCodigoQr() : "N/A"));
                        contentStream.endText();
                        currentY -= (lineHeight * 2);
                    }

                    // Instrucciones o pie de página
                    contentStream.beginText();
                    contentStream.setFont(fontRegular, 9);
                    contentStream.newLineAtOffset(margin, margin + lineHeight * 2); // Posicionar cerca del fondo
                    contentStream.showText("Presenta esta entrada (impresa o en tu dispositivo móvil) en el acceso al festival.");
                    contentStream.newLineAtOffset(0, -lineHeight);
                    contentStream.showText("Esta entrada es personal e intransferible una vez nominada. No se admiten devoluciones.");
                    contentStream.newLineAtOffset(0, -lineHeight);
                    contentStream.setFont(fontBold, 9);
                    contentStream.showText("Beatpass TFG - " + java.time.Year.now().getValue());
                    contentStream.endText();
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            log.info("PDF múltiple generado para {} entradas del festival '{}'", entradas.size(), nombreFestival);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error crítico generando PDF múltiple para festival '{}': {}", nombreFestival, e.getMessage(), e);
            throw e;
        }
    }
}
