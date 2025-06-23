package com.beatpass.web;

import com.beatpass.dto.CredencialesDTO;
import com.beatpass.dto.TokenDTO;
import com.beatpass.model.Usuario;
import com.beatpass.service.UsuarioService;
import com.beatpass.service.UsuarioServiceImpl;
import com.beatpass.util.JwtUtil;
import com.beatpass.util.PasswordUtil;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Recurso JAX-RS para autenticación (/api/auth). Proporciona el endpoint de
 * login para la API (devuelve JWT).
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger log = LoggerFactory.getLogger(AuthResource.class);

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public AuthResource() {
        this.usuarioService = new UsuarioServiceImpl();
        this.jwtUtil = new JwtUtil();
    }

    /**
     * Autentica un usuario y devuelve un JWT si es exitoso y la cuenta está
     * activa.
     *
     * @param credenciales DTO con email y contraseña.
     * @return 200 OK con TokenDTO, 400 Bad Request, 401 Unauthorized, o 500
     * Internal Server Error.
     */
    @POST
    @Path("/login")
    public Response login(@Valid CredencialesDTO credenciales) {
        log.info("Intento de login API para email: {}", credenciales != null ? credenciales.getEmail() : "null");

        if (credenciales == null || credenciales.getEmail() == null || credenciales.getEmail().isBlank()
                || credenciales.getPassword() == null || credenciales.getPassword().isEmpty()) {
            log.warn("Login API fallido: Credenciales incompletas.");
            throw new BadRequestException("Email y contraseña son obligatorios.");
        }

        Optional<Usuario> usuarioOpt = usuarioService.obtenerEntidadUsuarioPorEmailParaAuth(credenciales.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (!Boolean.TRUE.equals(usuario.getEstado())) {
                log.warn("Login API fallido: Cuenta inactiva para email {}", credenciales.getEmail());
                throw new NotAuthorizedException("Cuenta inactiva.");
            }

            if (PasswordUtil.checkPassword(credenciales.getPassword(), usuario.getPassword())) {
                log.info("Autenticación API exitosa para email: {}", credenciales.getEmail());

                String token = jwtUtil.generarToken(
                        usuario.getIdUsuario().toString(),
                        usuario.getRol().name(),
                        usuario.getNombre(),
                        usuario.getCambioPasswordRequerido()
                );

                TokenDTO tokenDTO = new TokenDTO();
                tokenDTO.setToken(token);
                return Response.ok(tokenDTO).build();

            } else {
                log.warn("Login API fallido: Contraseña incorrecta para email {}", credenciales.getEmail());
                throw new NotAuthorizedException("Email o contraseña incorrectos.");
            }
        } else {
            log.warn("Login API fallido: Usuario no encontrado con email {}", credenciales.getEmail());
            throw new NotAuthorizedException("Email o contraseña incorrectos.");
        }
    }
}
