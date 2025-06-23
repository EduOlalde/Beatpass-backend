package com.beatpass.util;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);
    private static final Properties mailProperties = new Properties();

    static {
        log.info("Iniciando bloque estático de MailConfig...");

        String host = System.getenv("MAIL_SMTP_HOST");
        String port = System.getenv("MAIL_SMTP_PORT");
        String auth = System.getenv("MAIL_SMTP_AUTH");
        String starttls = System.getenv("MAIL_SMTP_STARTTLS_ENABLE");
        String sslEnable = System.getenv("MAIL_SMTP_SSL_ENABLE");
        String debugMail = System.getenv("MAIL_SMTP_DEBUG");
        String user = System.getenv("MAIL_SMTP_USER"); // Para logging

        log.info("Leyendo variables de entorno para MailConfig:");
        log.info("MAIL_SMTP_HOST: {}", host);
        log.info("MAIL_SMTP_PORT: {}", port);
        log.info("MAIL_SMTP_USER: {}", user != null ? user.substring(0, Math.min(user.length(), 3)) + "***" : null); // No loguear todo el user
        log.info("MAIL_SMTP_AUTH: {}", auth);
        log.info("MAIL_SMTP_STARTTLS_ENABLE: {}", starttls);
        log.info("MAIL_SMTP_SSL_ENABLE: {}", sslEnable);
        log.info("MAIL_SMTP_DEBUG: {}", debugMail);

        if (host != null && !host.isBlank()) {
            mailProperties.put("mail.smtp.host", host);
        } else {
            log.error("¡CRÍTICO! Variable de entorno MAIL_SMTP_HOST no definida. El envío de emails podría intentar usar 'localhost'.");
            mailProperties.put("mail.smtp.host", "localhost"); // Default de JavaMail si no se especifica
        }

        if (port != null && !port.isBlank()) {
            mailProperties.put("mail.smtp.port", port);
        } else {
            log.warn("MAIL_SMTP_PORT no definida. JavaMail usará un puerto por defecto (ej. 25).");
            // No poner un default aquí, dejar que JavaMail use su defecto si el host no es localhost
        }

        mailProperties.put("mail.smtp.auth", String.valueOf(Boolean.parseBoolean(auth))); // Maneja null como "false"

        if (Boolean.parseBoolean(starttls)) {
            mailProperties.put("mail.smtp.starttls.enable", "true");
        } else {
            mailProperties.put("mail.smtp.starttls.enable", "false");
        }

        if (Boolean.parseBoolean(sslEnable)) {
            mailProperties.put("mail.smtp.ssl.enable", "true");
            if (Boolean.parseBoolean(starttls)) { // No deberían estar ambos activos
                log.warn("Tanto MAIL_SMTP_STARTTLS_ENABLE como MAIL_SMTP_SSL_ENABLE están 'true'. Esto puede causar problemas. Generalmente solo uno debe ser true.");
            }
        } else {
            mailProperties.put("mail.smtp.ssl.enable", "false");
        }

        String sslTrust = System.getenv("MAIL_SMTP_SSL_TRUST");
        if (sslTrust != null && !sslTrust.isBlank()) {
            mailProperties.put("mail.smtp.ssl.trust", sslTrust);
        } else if (host != null && host.toLowerCase().contains("gmail.com")) {
            mailProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            log.info("Estableciendo mail.smtp.ssl.trust a smtp.gmail.com por defecto para Gmail.");
        }

        String sslProtocols = System.getenv("MAIL_SMTP_SSL_PROTOCOLS");
        if (sslProtocols != null && !sslProtocols.isBlank()) {
            mailProperties.put("mail.smtp.ssl.protocols", sslProtocols);
        }

        if (Boolean.parseBoolean(debugMail)) {
            mailProperties.put("mail.debug", "true");
            log.info("Depuración de JavaMail HABILITADA (MAIL_SMTP_DEBUG=true).");
        }

        log.info("Propiedades de JavaMail configuradas en MailConfig: {}", mailProperties);
        log.info("MailConfig inicializado. SMTP Host final en props: {}", mailProperties.getProperty("mail.smtp.host"));
    }

    public static Properties getMailProperties() {
        Properties propsCopy = new Properties();
        propsCopy.putAll(mailProperties);
        log.debug("EmailServiceImpl está solicitando MailProperties. Devolviendo: {}", propsCopy);
        return propsCopy;
    }

    public static String getSmtpUser() {
        return System.getenv("MAIL_SMTP_USER");
    }

    public static String getSmtpPassword() {
        return System.getenv("MAIL_SMTP_PASSWORD");
    }

    public static String getFromAddress() {
        String from = System.getenv("MAIL_FROM_ADDRESS");
        if (from == null || from.isBlank()) {
            log.error("MAIL_FROM_ADDRESS no está configurado. Se usará un remitente por defecto, lo cual podría fallar.");
            return "noreply@example.com";
        }
        return from;
    }

    public static String getAppBaseUrl() {
        String url = System.getenv("APP_BASE_URL");
        if (url == null || url.isBlank()) {
            log.warn("APP_BASE_URL no está configurada. Los enlaces en los emails podrían estar rotos.");
            return "";
        }
        return url;
    }
}
