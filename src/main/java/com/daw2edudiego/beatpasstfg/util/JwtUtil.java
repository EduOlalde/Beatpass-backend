package com.daw2edudiego.beatpasstfg.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Import específico para claridad
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey; // Usar interfaz SecretKey
import java.util.Date;
import java.util.Objects; // Para comprobaciones null

/**
 * Clase de utilidad para generar y validar JSON Web Tokens (JWT).
 * <p>
 * Incluye el ID de usuario (como subject) y el rol de usuario (como claim
 * personalizado). Proporciona métodos para la creación de tokens, validación y
 * extracción de claims.
 * </p>
 * <p>
 * <strong>¡ADVERTENCIA DE SEGURIDAD!</strong> La clave secreta está actualmente
 * hardcodeada en esta clase. En un entorno de producción, esta clave DEBE
 * cargarse desde una fuente externa segura, como variables de entorno, un
 * servidor de configuración o un sistema de gestión de secretos. Nunca
 * confirmes claves secretas directamente en el control de versiones.
 * </p>
 *
 * @author Eduardo Olalde (modificado)
 */
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // --- Configuración del Token ---
    /**
     * !! RIESGO DE SEGURIDAD !! Clave secreta hardcodeada. Reemplazar con una
     * clave cargada de forma segura en producción. La longitud de la clave debe
     * ser apropiada para el algoritmo elegido (HS256 requiere >= 256 bits / 32
     * bytes).
     */
    private static final String SECRET_KEY_STRING = "MiClaveSuperSecretaLargaParaQueFuncioneBien1234567890"; // Ejemplo >= 32 bytes ASCII

    /**
     * Tiempo de expiración del token en milisegundos (ej., 1 hora).
     */
    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60; // 1 hora

    /**
     * Nombre del claim personalizado utilizado para almacenar el rol del
     * usuario dentro del payload del JWT.
     */
    private static final String ROLE_CLAIM_NAME = "role";

    // --- Instancia de la Clave Secreta (Generada una sola vez) ---
    // Usar la interfaz SecretKey para una mejor abstracción.
    // La clave se deriva de SECRET_KEY_STRING usando el algoritmo HMAC-SHA.
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    /**
     * Genera un JWT para un ID de usuario y rol dados.
     *
     * @param userId El ID del usuario (se establecerá como el 'subject' del
     * token). No puede ser nulo.
     * @param role El rol del usuario (ej: "ADMIN", "PROMOTOR"). Se añadirá como
     * claim personalizado. No puede ser nulo.
     * @return El JWT generado como una cadena compacta (String).
     * @throws NullPointerException si userId o role es nulo.
     */
    public String generarToken(String userId, String role) {
        Objects.requireNonNull(userId, "El ID de usuario no puede ser nulo para generar el token.");
        Objects.requireNonNull(role, "El Rol no puede ser nulo para generar el token.");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_MS);

        log.debug("Generando token para userId: {} con rol: {}", userId, role);

        return Jwts.builder()
                .setSubject(userId) // Establecer el identificador principal del usuario
                .claim(ROLE_CLAIM_NAME, role) // Añadir el rol como claim personalizado
                .setIssuedAt(now) // Timestamp de cuándo se emitió el token
                .setExpiration(expiryDate) // Timestamp de cuándo expira el token
                .signWith(key, SignatureAlgorithm.HS256) // Firmar el token usando la clave secreta y el algoritmo HS256
                .compact(); // Construir el token en una cadena compacta y segura para URL
    }

    /**
     * Valida una cadena JWT, comprobando su firma y expiración. Si es válido,
     * devuelve los claims contenidos dentro del token.
     *
     * @param token La cadena JWT a validar. No puede ser nula ni estar en
     * blanco.
     * @return El objeto {@link Claims} extraído del token si la validación
     * tiene éxito.
     * @throws ExpiredJwtException si el token ha expirado.
     * @throws UnsupportedJwtException si el formato del token no es soportado.
     * @throws MalformedJwtException si la cadena del token está mal formada.
     * @throws SignatureException si la firma del token es inválida (manipulado
     * o clave incorrecta).
     * @throws IllegalArgumentException si la cadena del token es nula, vacía o
     * solo contiene espacios en blanco.
     * @throws JwtException para otros errores de validación relacionados con
     * JWT.
     */
    public Claims validarTokenYObtenerClaims(String token) throws JwtException {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("La cadena del token no puede ser nula o estar en blanco.");
        }

        log.debug("Validando token y extrayendo claims...");
        try {
            // parserBuilder() se usa para crear una instancia de parser.
            // setSigningKey() especifica la clave a usar para la verificación de la firma.
            // build() crea el parser inmutable.
            // parseClaimsJws() parsea el token, verifica firma y expiración, y devuelve Jws<Claims>.
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            Claims body = claimsJws.getBody();
            log.debug("Token validado exitosamente. Subject: {}, Role: {}",
                    body.getSubject(),
                    body.get(ROLE_CLAIM_NAME, String.class));

            // Devolver el payload (Claims) del token
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
     * Extrae el ID de usuario (subject) de los claims. Asume que el objeto
     * claims proviene de un token previamente validado.
     *
     * @param claims El objeto {@link Claims} validado. No puede ser nulo.
     * @return El ID de usuario (claim subject).
     */
    public String obtenerUserIdDeClaims(Claims claims) {
        Objects.requireNonNull(claims, "El objeto Claims no puede ser nulo.");
        return claims.getSubject();
    }

    /**
     * Extrae el rol de usuario del claim de rol personalizado. Asume que el
     * objeto claims proviene de un token previamente validado.
     *
     * @param claims El objeto {@link Claims} validado. No puede ser nulo.
     * @return La cadena del rol del usuario.
     */
    public String obtenerRolDeClaims(Claims claims) {
        Objects.requireNonNull(claims, "El objeto Claims no puede ser nulo.");
        // Recuperar el rol usando el nombre de claim definido y el tipo esperado
        return claims.get(ROLE_CLAIM_NAME, String.class);
    }

    /**
     * Extrae la fecha de expiración de los claims. Asume que el objeto claims
     * proviene de un token previamente validado.
     *
     * @param claims El objeto {@link Claims} validado. No puede ser nulo.
     * @return La {@link Date} de expiración.
     */
    public Date obtenerExpiracionDeClaims(Claims claims) {
        Objects.requireNonNull(claims, "El objeto Claims no puede ser nulo.");
        return claims.getExpiration();
    }

    // --- Métodos Deprecados ---
    // Estos métodos se mantienen por compatibilidad hacia atrás pero idealmente deberían ser reemplazados
    // usando validarTokenYObtenerClaims() seguido por métodos específicos de extracción de claims.
    /**
     * Extrae el nombre de usuario (subject) de una cadena de token. Este método
     * realiza la validación internamente pero solo devuelve el subject. Suprime
     * excepciones, devolviendo null en caso de fallo, lo que podría ocultar
     * errores.
     *
     * @param token La cadena JWT.
     * @return El nombre de usuario (subject) si el token es válido y contiene
     * un subject, null en caso contrario.
     * @deprecated Usar {@link #validarTokenYObtenerClaims(String)} y luego
     * {@link #obtenerUserIdDeClaims(Claims)} para un mejor manejo de errores y
     * claridad.
     */
    @Deprecated
    public static String obtenerUsername(String token) {
        // Nota: Esto usa el campo estático key, lo que podría ser confuso si existe una instancia de JwtUtil.
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            log.warn("(Deprecado) Error obteniendo username del token: {}", e.getMessage());
            return null; // Suprime la causa real del error
        }
    }

    /**
     * Valida una cadena de token (firma y expiración) sin devolver claims.
     * Suprime excepciones, devolviendo un booleano, lo que podría ocultar la
     * razón de la invalidez.
     *
     * @param token La cadena JWT.
     * @return true si el token es válido, false en caso contrario.
     * @deprecated Usar {@link #validarTokenYObtenerClaims(String)} y manejar
     * las posibles {@link JwtException}s para resultados de validación
     * detallados.
     */
    @Deprecated
    public static boolean validarToken(String token) {
        // Nota: Esto usa el campo estático key.
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.warn("(Deprecado) Validación del token fallida: {}", e.getMessage());
            return false; // Oculta la razón específica del fallo
        }
    }

    /**
     * Constructor privado para prevenir la instanciación.
     */
    public JwtUtil() {
        // Clase de utilidad, no debe ser instanciada.
    }
}
