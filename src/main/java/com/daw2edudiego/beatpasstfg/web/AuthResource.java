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
    @POST
    @Path("/login")
    public Response login(@Valid CredencialesDTO credenciales) {
        log.info("Intento de login para email: {}", credenciales != null ? credenciales.getEmail() : "null");

        if (credenciales == null || credenciales.getEmail() == null || credenciales.getEmail().isBlank()
                || credenciales.getPassword() == null || credenciales.getPassword().isEmpty()) {
            log.warn("Login fallido: Credenciales incompletas.");
            return Response.status(Response.Status.BAD_REQUEST).entity("Email y contraseña son obligatorios.").build();
        }

        try {
            // 1. Buscar usuario por email usando el servicio (que usa el repositorio)
            //    Usamos el DTO aquí para no exponer la entidad completa si no es necesario
            Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerUsuarioPorEmail(credenciales.getEmail());

            if (usuarioOpt.isPresent()) {
                UsuarioDTO usuario = usuarioOpt.get();

                // 2. Verificar si la cuenta está activa
                if (!Boolean.TRUE.equals(usuario.getEstado())) {
                    log.warn("Login fallido: Cuenta inactiva para email {}", credenciales.getEmail());
                    return Response.status(Response.Status.UNAUTHORIZED).entity("Cuenta inactiva.").build();
                }

                // 3. Verificar contraseña (Necesitamos obtener el hash real)
                //    Para esto, el servicio o repositorio debería poder devolver la entidad completa o al menos el hash.
                //    Modificaremos UsuarioService conceptualmente para este ejemplo.
                Optional<String> hashOpt = obtenerHashPasswordPorEmail(credenciales.getEmail()); // Método auxiliar simulado

                if (hashOpt.isPresent() && PasswordUtil.checkPassword(credenciales.getPassword(), hashOpt.get())) {
                    // ¡Contraseña correcta!
                    log.info("Autenticación exitosa para email: {}", credenciales.getEmail());

                    // 4. Generar Token JWT
                    //    Pasamos ID y Rol al generador de tokens.
                    String token = jwtUtil.generarToken(
                            usuario.getIdUsuario().toString(), // Usualmente el ID como "subject"
                            usuario.getRol().name() // El rol como un "claim" personalizado
                    // Podrías añadir más claims si los necesitas
                    );

                    // 5. Devolver el token
                    return Response.ok(new TokenDTO(token)).build();
                } else {
                    // Contraseña incorrecta
                    log.warn("Login fallido: Contraseña incorrecta para email {}", credenciales.getEmail());
                    return Response.status(Response.Status.UNAUTHORIZED).entity("Email o contraseña incorrectos.").build();
                }
            } else {
                // Usuario no encontrado
                log.warn("Login fallido: Usuario no encontrado con email {}", credenciales.getEmail());
                return Response.status(Response.Status.UNAUTHORIZED).entity("Email o contraseña incorrectos.").build();
            }

        } catch (Exception e) {
            log.error("Error interno durante el login para email {}: {}", credenciales.getEmail(), e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al procesar el login.").build();
        }
    }

    /**
     * Método auxiliar SIMULADO para obtener el hash de la contraseña. En una
     * implementación real, esto debería ser parte de UsuarioService o
     * UsuarioRepository, devolviendo la entidad Usuario completa de forma
     * segura.
     */
    private Optional<String> obtenerHashPasswordPorEmail(String email) {
        // ¡¡¡ ESTO ES UNA SIMULACIÓN !!!
        // Deberías llamar a un método en tu UsuarioService/Repository
        // que devuelva la entidad Usuario completa y obtener el hash de ahí.
        // Ejemplo: Optional<Usuario> usuarioCompletoOpt = usuarioService.obtenerEntidadUsuarioPorEmail(email);
        //          return usuarioCompletoOpt.map(Usuario::getPassword);

        // Simulación para el ejemplo:
        if ("admin@beatpass.com".equals(email)) {
            return Optional.of(PasswordUtil.hashPassword("admin123"));
        }
        if ("promotor@beatpass.com".equals(email)) {
            return Optional.of(PasswordUtil.hashPassword("promotor123"));
        }
        return Optional.empty();
    }

}
