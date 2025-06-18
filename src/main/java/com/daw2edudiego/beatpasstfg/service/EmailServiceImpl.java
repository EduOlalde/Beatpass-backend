package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.util.MailConfig;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final PdfService pdfService;

    public EmailServiceImpl() {
        this.pdfService = new PdfServiceImpl();
    }

    @Override
    public void enviarEmailEntradasCompradas(String destinatarioEmail, String nombreComprador, String nombreFestival, List<EntradaDTO> entradasCompradas) {
        if (!esValidoParaEnviar(destinatarioEmail, nombreFestival, entradasCompradas, "Compra")) {
            return;
        }

        String asunto = "Beatpass - Confirmación y Entradas para " + nombreFestival;
        String contenidoHtml = construirHtmlEntradasCompradas(nombreComprador, nombreFestival, entradasCompradas);
        byte[] pdfBytes = null;
        String nombreArchivoPdf = "Entradas_" + nombreFestival.replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";

        try {
            if (pdfService != null) {
                pdfBytes = pdfService.generarPdfMultiplesEntradas(entradasCompradas, nombreFestival);
            } else {
                log.warn("PdfService no está inicializado. No se generará PDF para email a {}", destinatarioEmail);
            }
        } catch (IOException e) {
            log.error("Error al generar PDF para email de compra a {}: {}", destinatarioEmail, e.getMessage(), e);
        }

        enviarEmailConAdjuntoOpcional(destinatarioEmail, asunto, contenidoHtml, pdfBytes, nombreArchivoPdf);
        log.info("Email de 'entradas compradas' (intento de envío) a {} para festival {}", destinatarioEmail, nombreFestival);
    }

    @Override
    public void enviarEmailEntradaNominada(String destinatarioEmail, String nombreNominado, EntradaDTO entradaNominada) {
        if (entradaNominada == null || entradaNominada.getNombreFestival() == null) {
            log.warn("Datos de entrada nominada incompletos para enviar email a {}.", destinatarioEmail);
            return;
        }
        if (!esValidoParaEnviar(destinatarioEmail, entradaNominada.getNombreFestival(), List.of(entradaNominada), "Nominación")) {
            return;
        }

        String asunto = "Beatpass - ¡Tienes una entrada para " + entradaNominada.getNombreFestival() + "!";
        String contenidoHtml = construirHtmlEntradaNominada(nombreNominado, entradaNominada);
        byte[] pdfBytes = null;
        String nombreArchivoPdf = "Tu_Entrada_" + entradaNominada.getNombreFestival().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";

        try {
            if (pdfService != null) {
                pdfBytes = pdfService.generarPdfMultiplesEntradas(List.of(entradaNominada), entradaNominada.getNombreFestival());
            } else {
                log.warn("PdfService no está inicializado. No se generará PDF para email a {}", destinatarioEmail);
            }
        } catch (IOException e) {
            log.error("Error al generar PDF para email de nominación a {}: {}", destinatarioEmail, e.getMessage(), e);
        }

        enviarEmailConAdjuntoOpcional(destinatarioEmail, asunto, contenidoHtml, pdfBytes, nombreArchivoPdf);
        log.info("Email de 'entrada nominada' (intento de envío) a {} para festival {}", destinatarioEmail, entradaNominada.getNombreFestival());
    }

    private boolean esValidoParaEnviar(String email, String nombreFestival, List<EntradaDTO> entradas, String tipoEmail) {
        if (email == null || email.isBlank()) {
            log.warn("Destinatario email nulo o vacío. No se enviará correo de {}.", tipoEmail);
            return false;
        }
        if (nombreFestival == null || nombreFestival.isBlank()) {
            log.warn("Nombre del festival nulo o vacío para email de {} a {}. No se enviará correo.", tipoEmail, email);
            return false;
        }
        if (entradas == null || entradas.isEmpty()) {
            log.warn("Lista de entradas vacía o nula para email de {} a {}. No se enviará correo.", tipoEmail, email);
            return false;
        }
        String mailHost = MailConfig.getMailProperties().getProperty("mail.smtp.host");
        if (MailConfig.getFromAddress() == null || MailConfig.getFromAddress().isBlank() || mailHost == null || mailHost.isBlank()) {
            log.error("Configuración de correo incompleta (remitente u host SMTP no definidos). No se puede enviar email de {} a {}.", tipoEmail, email);
            return false;
        }
        return true;
    }

    private void enviarEmailConAdjuntoOpcional(String destinatarioEmail, String asunto, String contenidoHtml, byte[] adjuntoBytes, String nombreArchivoAdjunto) {
        Properties props = MailConfig.getMailProperties();
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (Boolean.parseBoolean(props.getProperty("mail.smtp.auth", "false"))) {
                    String smtpUser = MailConfig.getSmtpUser();
                    String smtpPassword = MailConfig.getSmtpPassword();
                    if (smtpUser != null && !smtpUser.isBlank() && smtpPassword != null && !smtpPassword.isBlank()) {
                        return new PasswordAuthentication(smtpUser, smtpPassword);
                    } else {
                        log.warn("Autenticación SMTP (mail.smtp.auth=true) pero credenciales SMTP incompletas en variables de entorno. El envío podría fallar.");
                    }
                }
                return null;
            }
        });

        if ("true".equalsIgnoreCase(props.getProperty("mail.debug"))) {
            session.setDebug(true);
        }

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MailConfig.getFromAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatarioEmail));
            message.setSubject(asunto);

            MimeBodyPart htmlBodyPart = new MimeBodyPart();
            htmlBodyPart.setContent(contenidoHtml, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart("mixed");
            multipart.addBodyPart(htmlBodyPart);

            if (adjuntoBytes != null && adjuntoBytes.length > 0) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(adjuntoBytes, "application/pdf");
                attachmentBodyPart.setDataHandler(new DataHandler(source));
                attachmentBodyPart.setFileName(nombreArchivoAdjunto);
                multipart.addBodyPart(attachmentBodyPart);
                log.debug("Adjunto {} preparado para email a {}", nombreArchivoAdjunto, destinatarioEmail);
            } else {
                log.warn("No hay bytes de adjunto para el email a {} (Asunto: {}). Se enviará sin adjunto.", destinatarioEmail, asunto);
            }

            message.setContent(multipart);
            Transport.send(message);

        } catch (MessagingException e_msg) {
            log.error("Error de mensajería al enviar email a {}: Asunto: '{}'. Error: {}", destinatarioEmail, asunto, e_msg.getMessage(), e_msg);
        } catch (Exception e_gen) {
            log.error("Error inesperado al construir o enviar email a {}: Asunto: '{}'. Error: {}", destinatarioEmail, asunto, e_gen.getMessage(), e_gen);
        }
    }

    private String construirHtmlEntradasCompradas(String nombreComprador, String nombreFestival, List<EntradaDTO> entradas) {
        StringBuilder sb = new StringBuilder();
        String appBaseUrl = MailConfig.getAppBaseUrl();

        if (appBaseUrl != null && !appBaseUrl.isEmpty() && !appBaseUrl.endsWith("/")) {
            appBaseUrl += "/";
        } else if (appBaseUrl == null || appBaseUrl.isEmpty()) {
            log.error("APP_BASE_URL no está configurada. Usando fallback que podría ser incorrecto: http://localhost:8080/BeatpassTFG/");
            appBaseUrl = "http://localhost:8080/BeatpassTFG/";
        }

        sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Tus Entradas para ").append(escapeHtml(nombreFestival)).append("</title>");
        appendBasicStyles(sb);
        sb.append("</head><body><div class='email-wrapper'><div class='email-header'><h1>Beatpass</h1></div>");
        sb.append("<div class='email-body'>");
        sb.append("<h2>¡Hola ").append(escapeHtml(nombreComprador)).append("!</h2>");
        sb.append("<p>Gracias por tu compra. Aquí tienes tus entradas para el festival: <strong>").append(escapeHtml(nombreFestival)).append("</strong>.</p>");
        sb.append("<p>Hemos adjuntado un archivo PDF con todas tus entradas. Por favor, revisa las instrucciones para cada una a continuación:</p>");

        boolean hayEntradasNominativas = false;

        sb.append("<ul>");
        for (EntradaDTO entrada : entradas) {
            sb.append("<li>");

            sb.append("Tipo: <span class='ticket-detail'>").append(escapeHtml(entrada.getTipoEntradaOriginal())).append("</span>");
            sb.append(" - Código: <code>").append(escapeHtml(entrada.getCodigoQr())).append("</code><br>");

            if (Boolean.TRUE.equals(entrada.getRequiereNominacion())) {
                hayEntradasNominativas = true;
                String nominationLink = appBaseUrl + "api/public/venta/nominar-entrada/" + entrada.getCodigoQr();
                sb.append("<span class='highlight'>Esta entrada requiere ser nominada.</span> Utiliza el siguiente botón para asignar un asistente:");
                sb.append(" <a href='").append(nominationLink).append("' class='button' style='font-size:0.8em; padding: 5px 10px; margin-left:10px;'>Nominar esta entrada</a>");
            } else {
                sb.append("<span style='color: #059669; font-weight: bold;'>Esta entrada es al portador y no requiere nominación.</span> Puedes entregarla directamente o usarla tú mismo.");
            }

            sb.append("</li>");
        }
        sb.append("</ul>");

        if (hayEntradasNominativas) {
            sb.append("<p>Recuerda que una vez nominada una entrada, el asistente recibirá su propia copia en su correo.</p>");
        }

        appendFooter(sb);
        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String construirHtmlEntradaNominada(String nombreNominado, EntradaDTO entrada) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>¡Tienes una Entrada!</title>");
        appendBasicStyles(sb);
        sb.append("</head><body><div class='email-wrapper'><div class='email-header'><h1>Beatpass</h1></div>");
        sb.append("<div class='email-body'>");
        sb.append("<h2>¡Hola ").append(escapeHtml(nombreNominado)).append("!</h2>");
        sb.append("<p>¡Buenas noticias! Se te ha asignado una entrada para el festival: <strong>").append(escapeHtml(entrada.getNombreFestival())).append("</strong>.</p>");
        sb.append("<p>Adjunto a este correo encontrarás tu entrada personalizada en formato PDF. Contiene la siguiente información:</p>");
        sb.append("<ul>");
        sb.append("<li>Festival: <span class='ticket-detail'>").append(escapeHtml(entrada.getNombreFestival())).append("</span></li>");
        sb.append("<li>Tipo de Entrada: <span class='ticket-detail'>").append(escapeHtml(entrada.getTipoEntradaOriginal())).append("</span></li>");
        sb.append("<li>A nombre de: <span class='ticket-detail'>").append(escapeHtml(nombreNominado)).append("</span></li>");
        sb.append("<li>Código QR: <code>").append(escapeHtml(entrada.getCodigoQr())).append("</code> (este es tu identificador único para la entrada).</li>");
        sb.append("</ul>");
        sb.append("<p>Por favor, descarga el PDF adjunto. Deberás presentarlo (impreso o en tu dispositivo móvil) para acceder al evento.</p>");

        String urlWebFestival = "https://daaf292.github.io/MockFestival_beatpass/"; // URL del MockFestival (puede ser una constante o configuración)
        if (!"#".equals(urlWebFestival)) {
            sb.append("<p>Para más información sobre el festival, como horarios y cartel, puedes visitar: <a href='").append(urlWebFestival).append("'>Información del Festival</a>.</p>");
        }

        appendFooter(sb);
        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private void appendBasicStyles(StringBuilder sb) {
        sb.append("<style>");
        sb.append("body{font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;margin:0;padding:0;background-color:#f0f2f5;color:#333;font-size:16px;line-height:1.6;}");
        sb.append(".email-wrapper{max-width:680px;margin:20px auto;background-color:#ffffff;border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;}");
        sb.append(".email-header{background-color:#7e22ce;color:#ffffff;padding:30px 20px;text-align:center;}");
        sb.append(".email-header h1{margin:0;font-size:28px;font-weight:600;}");
        sb.append(".email-body{padding:25px 30px;}");
        sb.append(".email-body h2{color:#7e22ce;font-size:22px;margin-top:0;margin-bottom:15px;}");
        sb.append(".email-body p{margin-bottom:15px;}");
        sb.append(".email-body ul{list-style-type:disc;margin-left:20px;margin-bottom:15px;padding-left:0;}");
        sb.append(".email-body li{margin-bottom:8px; line-height: 1.8;}");
        sb.append(".highlight{font-weight:bold;color:#7e22ce;}");
        sb.append(".ticket-detail{font-weight:bold;}");
        sb.append(".button-container{text-align:center;margin-top:25px;margin-bottom:15px;}");
        sb.append(".button{display:inline-block;padding:10px 18px;background-color:#F59E0B;color:#ffffff !important;text-decoration:none;border-radius:5px;font-weight:bold;font-size:14px;}");
        sb.append(".button:hover{background-color:#D97706;}");
        sb.append(".email-footer{background-color:#f7f7f7;padding:20px;text-align:center;font-size:12px;color:#777777;border-top:1px solid #e0e0e0;}");
        sb.append("code{background-color:#f0f0f0;padding:2px 4px;border-radius:3px;font-family:monospace;font-size:0.9em;}");
        sb.append("a{color:#7e22ce;text-decoration:none;}a:hover{text-decoration:underline;}");
        sb.append("</style>");
    }

    private void appendFooter(StringBuilder sb) {
        sb.append("<p>¡Disfruta del festival!</p>");
        sb.append("<p>Atentamente,<br>El equipo de Beatpass</p>");
        sb.append("<div class='email-footer'><p>&copy; ").append(java.time.Year.now().getValue()).append(" Beatpass. Todos los derechos reservados.</p><p>Este es un correo electrónico generado automáticamente. Por favor, no respondas directamente a este mensaje.</p></div>"); //
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
