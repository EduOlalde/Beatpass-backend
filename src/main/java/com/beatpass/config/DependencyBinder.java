package com.beatpass.config;

import com.beatpass.repository.*;
import com.beatpass.service.*;
import jakarta.inject.Singleton;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Registra las implementaciones de servicios y repositorios para la inyección
 * de dependencias (HK2). Esto permite que Jersey gestione el ciclo de vida de
 * nuestros componentes y los inyecte donde se necesiten.
 */
public class DependencyBinder extends AbstractBinder {

    @Override
    protected void configure() {
        // --- Bindeo de Repositorios ---
        bind(AsistenteRepositoryImpl.class).to(AsistenteRepository.class).in(Singleton.class);
        bind(CompradorRepositoryImpl.class).to(CompradorRepository.class).in(Singleton.class);
        bind(CompraRepositoryImpl.class).to(CompraRepository.class).in(Singleton.class);
        bind(CompraEntradaRepositoryImpl.class).to(CompraEntradaRepository.class).in(Singleton.class);
        bind(ConsumoRepositoryImpl.class).to(ConsumoRepository.class).in(Singleton.class);
        bind(EntradaRepositoryImpl.class).to(EntradaRepository.class).in(Singleton.class);
        bind(FestivalRepositoryImpl.class).to(FestivalRepository.class).in(Singleton.class);
        bind(PulseraNFCRepositoryImpl.class).to(PulseraNFCRepository.class).in(Singleton.class);
        bind(RecargaRepositoryImpl.class).to(RecargaRepository.class).in(Singleton.class);
        bind(TipoEntradaRepositoryImpl.class).to(TipoEntradaRepository.class).in(Singleton.class);
        bind(UsuarioRepositoryImpl.class).to(UsuarioRepository.class).in(Singleton.class);

        // --- Bindeo de Servicios ---
        bind(AsistenteServiceImpl.class).to(AsistenteService.class).in(Singleton.class);
        bind(CompradorServiceImpl.class).to(CompradorService.class).in(Singleton.class);
        bind(CompraServiceImpl.class).to(CompraService.class).in(Singleton.class);
        bind(EmailServiceImpl.class).to(EmailService.class).in(Singleton.class);
        bind(EntradaServiceImpl.class).to(EntradaService.class).in(Singleton.class);
        bind(FestivalServiceImpl.class).to(FestivalService.class).in(Singleton.class);
        bind(PdfServiceImpl.class).to(PdfService.class).in(Singleton.class);
        bind(PulseraNFCServiceImpl.class).to(PulseraNFCService.class).in(Singleton.class);
        bind(TipoEntradaServiceImpl.class).to(TipoEntradaService.class).in(Singleton.class);
        bind(UsuarioServiceImpl.class).to(UsuarioService.class).in(Singleton.class);
        bind(VentaServiceImpl.class).to(VentaService.class).in(Singleton.class);
    }
}
