/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.util;

import io.jsonwebtoken.*;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Import específico para claridad
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey; // Usar interfaz SecretKey
import java.util.Date;

/**
 * Utilidad para generar y validar JSON Web Tokens (JWT). Incluye el ID de
 * usuario (subject) y el rol (claim personalizado).
 *
 * ¡ADVERTENCIA DE SEGURIDAD! La clave secreta está hardcodeada. En producción,
 * cárgala desde una fuente segura (variables de entorno, config externa).
 *
 * @author Eduardo Olalde (modificado)
 */
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // --- Configuración del Token ---
    // ¡¡¡ NUNCA hardcodear la clave secreta en producción !!!
    // Debe tener una longitud adecuada para el algoritmo HS256 (256 bits / 32 bytes mínimo recomendado)
    private static final String SECRET_KEY_STRING = "MiClaveSuperSecretaLargaParaQueFuncioneBien1234567890"; // Ejemplo >= 32 bytes ASCII
    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60; // 1 hora en milisegundos
    private static final String ROLE_CLAIM_NAME = "role"; // Nombre del claim para el rol

    // --- Clave Secreta (Generada una sola vez) ---
    // Usar la interfaz SecretKey es más genérico
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    /**
     * Genera un token JWT para un usuario con su ID y rol.
     *
     * @param userId El ID del usuario (se usará como 'subject' del token).
     * @param role El rol del usuario (ej: "ADMIN", "PROMOTOR"). Se añadirá como
     * claim.
     * @return El token JWT generado como String.
     */
    public String generarToken(String userId, String role) {
        if (userId == null || role == null) {
            log.error("Intento de generar token con userId o role nulos.");
            throw new IllegalArgumentException("User ID y Role no pueden ser nulos para generar el token.");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_MS);

        log.debug("Generando token para usuario ID: {} con rol: {}", userId, role);

        return Jwts.builder()
                .setSubject(userId) // Identificador principal del usuario
                .claim(ROLE_CLAIM_NAME, role) // Añadir el rol como claim personalizado
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256) // Usar la clave y el algoritmo
                .compact();
    }

    /**
     * Valida un token JWT y devuelve los claims si es válido. Verifica la firma
     * y la expiración.
     *
     * @param token El token JWT como String.
     * @return Los Claims extraídos del token si es válido.
     * @throws ExpiredJwtException Si el token ha expirado.
     * @throws UnsupportedJwtException Si el token no tiene el formato esperado.
     * @throws MalformedJwtException Si el token está mal formado.
     * @throws SignatureException Si la firma del token es inválida.
     * @throws IllegalArgumentException Si el token es nulo o vacío.
     */
    public Claims validarTokenYObtenerClaims(String token) throws JwtException {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede ser nulo o vacío.");
        }

        log.debug("Validando token y obteniendo claims...");
        try {
            // parseClaimsJws valida firma y expiración y devuelve Jws<Claims>
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key) // Establece la clave para verificar la firma
                    .build()
                    .parseClaimsJws(token); // Parsea y valida

            log.debug("Token validado exitosamente. Subject: {}, Role: {}",
                    claimsJws.getBody().getSubject(),
                    claimsJws.getBody().get(ROLE_CLAIM_NAME, String.class));

            // Devolvemos el cuerpo (Claims)
            return claimsJws.getBody();

        } catch (ExpiredJwtException eje) {
            log.warn("Token expirado: {}", eje.getMessage());
            throw eje; // Relanzar para que el filtro sepa que expiró
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            // Englobar otros errores de validación/parseo
            log.warn("Token inválido: {}", e.getMessage());
            throw new JwtException("Token inválido: " + e.getMessage(), e); // Relanzar como JwtException genérica o específica
        }
        // No debería llegar aquí si todo va bien o si hay excepción
    }

    /**
     * Extrae el ID de usuario (subject) de los claims. Asume que los claims ya
     * han sido validados.
     *
     * @param claims Los claims obtenidos de un token válido.
     * @return El ID de usuario (subject).
     */
    public String obtenerUserIdDeClaims(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extrae el rol de usuario del claim personalizado. Asume que los claims ya
     * han sido validados.
     *
     * @param claims Los claims obtenidos de un token válido.
     * @return El rol del usuario.
     */
    public String obtenerRolDeClaims(Claims claims) {
        return claims.get(ROLE_CLAIM_NAME, String.class);
    }

    /**
     * Extrae la fecha de expiración de los claims. Asume que los claims ya han
     * sido validados.
     *
     * @param claims Los claims obtenidos de un token válido.
     * @return La fecha de expiración.
     */
    public Date obtenerExpiracionDeClaims(Claims claims) {
        return claims.getExpiration();
    }

    // --- Métodos anteriores (opcional mantenerlos si se usan en otro lugar) ---
    /**
     * @deprecated Usar validarTokenYObtenerClaims y luego obtenerUserIdDeClaims
     */
    @Deprecated
    public static String obtenerUsername(String token) {
        // Esta implementación es similar a validarTokenYObtenerClaims pero solo devuelve el subject
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            log.warn("Error al obtener username del token: {}", e.getMessage());
            return null; // O lanzar excepción
        }
    }

    /**
     * @deprecated Usar validarTokenYObtenerClaims y manejar excepciones
     */
    @Deprecated
    public static boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
