package com.gilede.livraria.scheduler;

import com.gilede.livraria.service.MercadoLivreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MercadoLivreScheduler {

    private final MercadoLivreService mercadoLivreService;

    @Scheduled(cron = "0 0 6 * * *", zone = "America/Sao_Paulo")
    public void syncCatalog() {
        log.info("Starting scheduled Mercado Livre sync");
        int total = mercadoLivreService.syncCatalog();
        log.info("Scheduled Mercado Livre sync finished with {} books processed", total);
    }
}