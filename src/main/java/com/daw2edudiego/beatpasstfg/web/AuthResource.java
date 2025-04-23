/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.CredencialesDTO;
import com.daw2edudiego.beatpasstfg.dto.TokenDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.service.UsuarioService;
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;
import com.daw2edudiego.beatpasstfg.util.JwtUtil; // ¡Necesitas implementar esta clase!
import com.daw2edudiego.beatpasstfg.util.PasswordUtil;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Recurso JAX-RS para manejar la autenticación (login).
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger log = LoggerFactory.getLogger(AuthResource.class);

    // Instanciación manual (o inyección si usaras CDI)
    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil; // ¡Asume que esta clase existe y está implementada!

    public AuthResource() {
        this.usuarioService = new UsuarioServiceImpl();
        this.jwtUtil = new JwtUtil(); // ¡Necesitas tu implementación aquí!
    }

    /**
     * Endpoint para autenticar un usuario y devolver un token JWT. POST
     * /api/auth/login
     */
    // Dentro de AuthResource.java
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON) // Asegúrate que consume JSON
    @Produces(MediaType.APPLICATION_JSON) // Y produce JSON
    public Response login(@Valid CredencialesDTO credenciales) {
        log.info("Intento de login para email: {}", credenciales != null ? credenciales.getEmail() : "null");

        if (credenciales == null || credenciales.getEmail() == null || credenciales.getEmail().isBlank()
                || credenciales.getPassword() == null || credenciales.getPassword().isEmpty()) {
            log.warn("Login fallido: Credenciales incompletas.");
            // Devolver error JSON estándar
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Email y contraseña son obligatorios.\"}")
                    .build();
        }

        try {
            // 1. Buscar la *entidad* completa del usuario por email
            Optional<com.daw2edudiego.beatpasstfg.model.Usuario> usuarioCompletoOpt
                    = usuarioService.obtenerEntidadUsuarioPorEmailParaAuth(credenciales.getEmail());

            if (usuarioCompletoOpt.isPresent()) {
                com.daw2edudiego.beatpasstfg.model.Usuario usuario = usuarioCompletoOpt.get();

                // 2. Verificar si la cuenta está activa
                if (!Boolean.TRUE.equals(usuario.getEstado())) {
                    log.warn("Login fallido: Cuenta inactiva para email {}", credenciales.getEmail());
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\": \"Cuenta inactiva.\"}")
                            .build();
                }

                // 3. Verificar contraseña usando PasswordUtil.checkPassword
                if (PasswordUtil.checkPassword(credenciales.getPassword(), usuario.getPassword())) {
                    // ¡Contraseña correcta!
                    log.info("Autenticación exitosa para email: {}", credenciales.getEmail());

                    // 4. Generar Token JWT
                    String token = jwtUtil.generarToken(
                            usuario.getIdUsuario().toString(),
                            usuario.getRol().name()
                    );

                    // 5. Devolver el token
                    return Response.ok(new TokenDTO(token)).build();
                } else {
                    // Contraseña incorrecta
                    log.warn("Login fallido: Contraseña incorrecta para email {}", credenciales.getEmail());
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\": \"Email o contraseña incorrectos.\"}")
                            .build();
                }
            } else {
                // Usuario no encontrado
                log.warn("Login fallido: Usuario no encontrado con email {}", credenciales.getEmail());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Email o contraseña incorrectos.\"}")
                        .build();
            }

        } catch (Exception e) {
            log.error("Error interno durante el login para email {}: {}", credenciales.getEmail(), e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error interno al procesar el login.\"}")
                    .build();
        }
    }

}
