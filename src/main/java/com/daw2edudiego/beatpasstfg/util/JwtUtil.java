package com.daw2edudiego.beatpasstfg.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;

/**
 * Clase de utilidad para generar y validar JSON Web Tokens (JWT). La clave
 * secreta debe cargarse de forma segura (variable de entorno TFG_TOKEN_KEY).
 */
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private static final String SECRET_KEY_STRING = System.getenv("TFG_TOKEN_KEY");
    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60; // 1 hora
    private static final String ROLE_CLAIM_NAME = "role";
    private static final SecretKey key;

    static {
        if (SECRET_KEY_STRING == null || SECRET_KEY_STRING.length() < 32) {
            log.error("¡¡¡ERROR FATAL!!! La variable de entorno TFG_TOKEN_KEY no está definida o es demasiado corta (< 32 bytes).");
            // Considerar lanzar una excepción aquí si es apropiado para el ciclo de vida de la app
            // throw new RuntimeException("TFG_TOKEN_KEY inválida.");
            key = null; // Dejar la llave nula para que falle después
        } else {
            key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
            log.info("Clave secreta JWT inicializada desde TFG_TOKEN_KEY.");
        }
    }

    /**
     * Genera un JWT para un ID de usuario y rol dados.
     *
     * @param userId El ID del usuario (subject). No puede ser nulo.
     * @param role El rol del usuario (claim). No puede ser nulo.
     * @return El JWT generado como String.
     * @throws NullPointerException si la clave secreta no pudo inicializarse.
     * @throws IllegalArgumentException si userId o role es nulo.
     */
    public String generarToken(String userId, String role) {
        Objects.requireNonNull(userId, "El ID de usuario no puede ser nulo para generar el token.");
        Objects.requireNonNull(role, "El Rol no puede ser nulo para generar el token.");
        if (key == null) {
            throw new IllegalStateException("La clave secreta JWT no está inicializada. Revisa las variables de entorno.");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_MS);

        log.debug("Generando token para userId: {} con rol: {}", userId, role);

        return Jwts.builder()
                .setSubject(userId)
                .claim(ROLE_CLAIM_NAME, role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida una cadena JWT (firma y expiración) y devuelve los claims.
     *
     * @param token La cadena JWT a validar. No puede ser nula/vacía.
     * @return El objeto Claims extraído si es válido.
     * @throws JwtException Si el token es inválido, expirado, mal formado, etc.
     * @throws NullPointerException si la clave secreta no pudo inicializarse.
     * @throws IllegalArgumentException si la cadena del token es nula o vacía.
     */
    public Claims validarTokenYObtenerClaims(String token) throws JwtException {
        if (key == null) {
            throw new IllegalStateException("La clave secreta JWT no está inicializada. Revisa las variables de entorno.");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("La cadena del token no puede ser nula o estar en blanco.");
        }

        log.debug("Validando token y extrayendo claims...");
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            Claims body = claimsJws.getBody();
            log.debug("Token validado exitosamente. Subject: {}, Role: {}",
                    body.getSubject(),
                    body.get(ROLE_CLAIM_NAME, String.class));
            return body;

        } catch (ExpiredJwtException eje) {
            log.warn("Validación del token fallida: Token expirado. {}", eje.getMessage());
            throw eje; // Relanzar excepción específica para manejo en otro lugar
        } catch (UnsupportedJwtException uje) {
            log.warn("Validación del token fallida: Formato JWT no soportado. {}", uje.getMessage());
            throw uje;
        } catch (MalformedJwtException mje) {
            log.warn("Validación del token fallida: Cadena JWT mal formada. {}", mje.getMessage());
            throw mje;
        } catch (SignatureException se) {
            log.warn("Validación del token fallida: Firma inválida. {}", se.getMessage());
            throw se;
        } catch (IllegalArgumentException iae) {
            // Esto podría capturar problemas durante la configuración del parseo, no solo token en blanco
            log.warn("Validación del token fallida: Argumento inválido durante el parseo. {}", iae.getMessage());
            throw iae;
        } catch (JwtException jwte) {
            // Captura genérica para otros errores específicos de la librería JWT
            log.warn("Validación del token fallida: Excepción JWT general. {}", jwte.getMessage());
            throw jwte;
        }
    }

    /**
     * Extrae el ID de usuario (subject) de los claims validados.
     *
     * @param claims El objeto Claims validado. No nulo.
     * @return El ID de usuario.
     */
    public String obtenerUserIdDeClaims(Claims claims) {
        Objects.requireNonNull(claims, "El objeto Claims no puede ser nulo.");
        return claims.getSubject();
    }

    /**
     * Extrae el rol de usuario del claim personalizado.
     *
     * @param claims El objeto Claims validado. No nulo.
     * @return La cadena del rol del usuario.
     */
    public String obtenerRolDeClaims(Claims claims) {
        Objects.requireNonNull(claims, "El objeto Claims no puede ser nulo.");
        return claims.get(ROLE_CLAIM_NAME, String.class);
    }

    /**
     * Extrae la fecha de expiración de los claims.
     *
     * @param claims El objeto Claims validado. No nulo.
     * @return La fecha de expiración.
     */
    public Date obtenerExpiracionDeClaims(Claims claims) {
        Objects.requireNonNull(claims, "El objeto Claims no puede ser nulo.");
        return claims.getExpiration();
    }

    // --- Métodos Deprecados Eliminados ---
    // Constructor privado para prevenir instanciación de clase utilidad si se prefiere estática
    // public JwtUtil() {} // Se deja público si se va a instanciar
}
