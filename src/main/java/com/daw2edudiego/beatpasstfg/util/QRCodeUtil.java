package com.daw2edudiego.beatpasstfg.util;

import java.util.UUID;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para generar contenido único e imágenes de códigos QR.
 * Utiliza la librería ZXing.
 */
public class QRCodeUtil {

    private static final Logger log = LoggerFactory.getLogger(QRCodeUtil.class);
    private static final String QR_PREFIX = "BEATPASS-TICKET-";

    /**
     * Genera una cadena identificadora única para códigos QR usando un prefijo
     * y UUID.
     *
     * @return Una cadena única (ej., "BEATPASS-TICKET-uuid").
     */
    public static String generarContenidoQrUnico() {
        UUID uuid = UUID.randomUUID();
        String uniqueContent = QR_PREFIX + uuid.toString();
        log.debug("Generado contenido QR único: {}", uniqueContent);
        return uniqueContent;
    }

    /**
     * Genera una imagen QR como URL de datos Base64
     * ("data:image/png;base64,...").
     *
     * @param qrContent El contenido a codificar (no nulo/vacío).
     * @param width Ancho deseado en píxeles (>0).
     * @param height Alto deseado en píxeles (>0).
     * @return La URL de datos Base64 de la imagen PNG, o null si falla.
     */
    public static String generarQrComoBase64(String qrContent, int width, int height) {
        if (qrContent == null || qrContent.isEmpty()) {
            log.warn("No se puede generar código QR para contenido nulo o vacío.");
            return null;
        }
        if (width <= 0 || height <= 0) {
            log.warn("No se puede generar código QR con dimensiones no positivas ({}x{}).", width, height);
            return null;
        }

        try {
            String qrContentLog = qrContent.substring(0, Math.min(30, qrContent.length())) + "...";
            log.trace("Generando imagen QR ({}x{}) para contenido: {}", width, height, qrContentLog);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            String base64Image = Base64.getEncoder().encodeToString(pngData);
            String dataUrl = "data:image/png;base64," + base64Image;
            log.trace("Imagen QR generada como URL de datos Base64 (longitud: {} caracteres).", dataUrl.length());
            return dataUrl;

        } catch (WriterException | IOException e) {
            log.error("Error generando imagen de código QR para contenido '{}': {}", qrContent, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Error inesperado generando imagen de código QR para contenido '{}': {}", qrContent, e.getMessage(), e);
            return null;
        }
    }

    // Prevenir instanciación
    private QRCodeUtil() {
    }
}
