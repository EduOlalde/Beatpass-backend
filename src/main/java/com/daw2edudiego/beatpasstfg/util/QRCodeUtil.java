package com.daw2edudiego.beatpasstfg.util;

import java.util.UUID;
// Imports para generación de imágenes QR
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
 * Clase de utilidad para generar contenido único para códigos QR y también para
 * generar la imagen QR como URL de datos Base64.
 * <p>
 * Destinado principalmente a identificar y visualizar entradas asignadas
 * ({@link com.daw2edudiego.beatpasstfg.model.EntradaAsignada}). Utiliza la
 * librería ZXing para la generación de imágenes.
 * </p>
 *
 * @see <a href="https://github.com/zxing/zxing">ZXing en GitHub</a>
 * @author Eduardo Olalde
 */
public class QRCodeUtil {

    /**
     * Logger para esta clase.
     */
    private static final Logger log = LoggerFactory.getLogger(QRCodeUtil.class);

    /**
     * Un prefijo añadido al identificador único generado para ayudar a
     * identificar los códigos QR originados en este sistema.
     */
    private static final String QR_PREFIX = "BEATPASS-TICKET-";

    /**
     * Genera una cadena identificadora única destinada a ser codificada en un
     * código QR para una entrada recién asignada.
     * <p>
     * Esta implementación utiliza un {@link UUID} (Universally Unique
     * Identifier) estándar prefijado con {@link #QR_PREFIX}. Los UUIDs
     * proporcionan una alta probabilidad de unicidad.
     * </p>
     *
     * @return Una cadena única adecuada para el contenido del código QR (ej.,
     * "BEATPASS-TICKET-123e4567-e89b-12d3-a456-426614174000").
     */
    public static String generarContenidoQrUnico() {
        // Generar un UUID aleatorio de Tipo 4
        UUID uuid = UUID.randomUUID();
        String uniqueContent = QR_PREFIX + uuid.toString();
        log.debug("Generado contenido QR único: {}", uniqueContent);
        return uniqueContent;
    }

    /**
     * Genera una imagen de código QR para el contenido dado y la devuelve como
     * una cadena de URL de datos codificada en Base64 (ej.,
     * "data:image/png;base64,...").
     * <p>
     * Requiere las dependencias de ZXing (core y javase).
     * </p>
     *
     * @param qrContent El contenido de texto a codificar en el código QR. No
     * puede ser nulo ni vacío.
     * @param width El ancho deseado de la imagen QR en píxeles. Debe ser
     * positivo.
     * @param height El alto deseado de la imagen QR en píxeles. Debe ser
     * positivo.
     * @return Una cadena codificada en Base64 que representa la imagen PNG del
     * código QR, prefijada con "data:image/png;base64,", o {@code null} si la
     * generación falla o los parámetros son inválidos.
     */
    public static String generarQrComoBase64(String qrContent, int width, int height) {
        // Validación de parámetros de entrada
        if (qrContent == null || qrContent.isEmpty()) {
            log.warn("No se puede generar código QR para contenido nulo o vacío.");
            return null;
        }
        if (width <= 0 || height <= 0) {
            log.warn("No se puede generar código QR con ancho ({}) o alto ({}) no positivos.", width, height);
            return null;
        }

        try {
            // Loguear solo una parte del contenido por brevedad y posible sensibilidad
            String qrContentLog = qrContent.substring(0, Math.min(30, qrContent.length())) + "...";
            log.trace("Generando imagen de código QR ({}x{}) para contenido: {}", width, height, qrContentLog);

            // Crear el escritor de QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Codificar el contenido en una matriz de bits (BitMatrix)
            // Esto representa la estructura visual del código QR
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);

            // Escribir la BitMatrix a una imagen PNG en memoria usando un ByteArrayOutputStream
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            // Obtener los bytes de la imagen PNG generada
            byte[] pngData = pngOutputStream.toByteArray();

            // Codificar el array de bytes PNG a una cadena Base64
            String base64Image = Base64.getEncoder().encodeToString(pngData);

            // Formatear como una URL de datos estándar (RFC 2397)
            String dataUrl = "data:image/png;base64," + base64Image;
            log.trace("Imagen de código QR generada exitosamente como URL de datos Base64 (longitud: {} caracteres).", dataUrl.length());
            return dataUrl;

        } catch (WriterException e) {
            // Error específico de ZXing durante la codificación
            log.error("ZXing WriterException mientras se generaba código QR para contenido '{}': {}", qrContent, e.getMessage(), e);
            return null;
        } catch (IOException e) {
            // Error durante la escritura de la imagen PNG al stream en memoria
            log.error("IOException mientras se escribía PNG de código QR a stream para contenido '{}': {}", qrContent, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            // Captura genérica para cualquier otro error inesperado
            log.error("Error inesperado generando imagen de código QR para contenido '{}': {}", qrContent, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Constructor privado para prevenir la instanciación, ya que es una clase
     * de utilidad con métodos estáticos.
     */
    private QRCodeUtil() {
        // Clase de utilidad, no debe ser instanciada.
    }
}
