/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import com.daw2edudiego.beatpasstfg.web.FestivalResource;
import com.daw2edudiego.beatpasstfg.web.UsuarioResource;
import com.daw2edudiego.beatpasstfg.web.AuthResource;
import com.daw2edudiego.beatpasstfg.web.PromotorResource;

import com.daw2edudiego.beatpasstfg.security.AuthenticationFilter;
import com.daw2edudiego.beatpasstfg.web.AdminResource;
import com.daw2edudiego.beatpasstfg.web.PuntoVentaResource;

@ApplicationPath("/api") // Ruta base API REST
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        // Recursos REST API
        resources.add(FestivalResource.class);
        resources.add(UsuarioResource.class);
        resources.add(AuthResource.class);

        // Recurso para el panel de promotor (que usa JSPs)
        resources.add(PromotorResource.class);
        resources.add(AdminResource.class);
        resources.add(PuntoVentaResource.class);

        // Filtros y otros Providers
        resources.add(AuthenticationFilter.class);
        // Añadir ExceptionMappers, etc.

        return resources;
    }
}
