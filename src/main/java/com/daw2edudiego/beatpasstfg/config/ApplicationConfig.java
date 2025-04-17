/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.config;

import com.daw2edudiego.beatpasstfg.web.FestivalResource;
import com.daw2edudiego.beatpasstfg.web.UsuarioResource;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Configura la aplicación JAX-RS. Define la ruta base para todos los endpoints
 * de la API. Registra las clases de Recursos (y opcionalmente Providers como
 * ExceptionMappers).
 */
@ApplicationPath("/api") // Define la ruta base para toda la API REST (ej: http://localhost:8080/beatpass-tfg/api/...)
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        // Añade aquí todas tus clases Resource JAX-RS
        resources.add(FestivalResource.class);
        resources.add(UsuarioResource.class);
        // Añade otras clases como ExceptionMappers si las tienes
        // resources.add(FestivalNotFoundExceptionMapper.class);
        // resources.add(ConstraintViolationExceptionMapper.class); // Para Bean Validation
        // resources.add(JacksonJsonProvider.class); // Jersey suele registrarlo automáticamente si está en el classpath
        return resources;
    }

    // Alternativamente, puedes devolver un Set vacío y Jersey escaneará
    // el classpath en busca de clases anotadas con @Path y @Provider,
    // pero el registro explícito a veces es preferible.
    /*
     @Override
     public Set<Class<?>> getClasses() {
         return new HashSet<>(); // Dejar que Jersey escanee
     }
     */
}
