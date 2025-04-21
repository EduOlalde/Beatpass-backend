package com.daw2edudiego.beatpasstfg.util;

import java.util.UUID; // Para generar identificadores únicos universales

/**
 * Utilidad para generar contenido único para los códigos QR de las entradas. En
 * esta versión inicial, simplemente genera un UUID único.
 * @author Eduardo Olalde
 */
public class QRCodeUtil {

    private static final String QR_PREFIX = "BEATPASS-TICKET-"; // Prefijo para identificar nuestros QRs

    /**
     * Genera una cadena de texto única para ser codificada en un código QR para
     * una nueva entrada asignada.
     *
     * Actualmente, genera un UUID precedido por un prefijo. En el futuro,
     * podría incluir más información relevante y segura.
     *
     * @return Una cadena única para el código QR.
     */
    public static String generarContenidoQrUnico() {
        // Generar un UUID (Universally Unique Identifier) aleatorio
        UUID uuid = UUID.randomUUID();
        // Devolver el UUID como string, precedido por nuestro prefijo
        return QR_PREFIX + uuid.toString();
    }

    /**
     * (Opcional) Método futuro para generar la imagen QR como datos Base64.
     * Necesitaría las librerías zxing-core y zxing-javase.
     *
     * @param qrContent La cadena de texto a codificar.
     * @param width El ancho deseado de la imagen QR en píxeles.
     * @param height El alto deseado de la imagen QR en píxeles.
     * @return String con la imagen QR codificada en Base64 (formato
     * data:image/png;base64,...), o null si ocurre un error.
     */
    /*
    public static String generarQrComoBase64(String qrContent, int width, int height) {
        if (qrContent == null || qrContent.isEmpty()) {
            return null;
        }
        try {
            com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, com.google.zxing.BarcodeFormat.QR_CODE, width, height);

            java.io.ByteArrayOutputStream pngOutputStream = new java.io.ByteArrayOutputStream();
            com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            // Codificar en Base64 y añadir prefijo para data URL
            return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(pngData);

        } catch (com.google.zxing.WriterException | java.io.IOException e) {
            // Loggear el error
            // log.error("Error generando imagen QR para contenido '{}': {}", qrContent, e.getMessage(), e);
            System.err.println("Error generando imagen QR: " + e.getMessage());
            return null;
        }
    }
     */
}
