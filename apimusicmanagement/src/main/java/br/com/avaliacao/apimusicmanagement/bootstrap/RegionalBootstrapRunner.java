package br.com.avaliacao.apimusicmanagement.bootstrap;

import br.com.avaliacao.apimusicmanagement.config.integration.RegionalIntegrationProperties;
import br.com.avaliacao.apimusicmanagement.domain.service.RegionalSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class RegionalBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RegionalBootstrapRunner.class);

    private final RegionalSyncService regionalSyncService;
    private final RegionalIntegrationProperties integrationProperties;

    public RegionalBootstrapRunner(RegionalSyncService regionalSyncService, RegionalIntegrationProperties integrationProperties) {
        this.regionalSyncService = regionalSyncService;
        this.integrationProperties = integrationProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!integrationProperties.isSyncOnStartup()) {
            log.info("Sincronização de regionais no startup desabilitada (app.integrations.regionais.sync-on-startup=false).");
            return;
        }

        try {
            log.info("Srinconizando regionais no startup...");
            regionalSyncService.sync();
            log.info("Sincronização de regionais finalizada.");
        } catch (Exception e) {
            log.error("Falha ao sincronizar regionais no startup. A aplicação continuará em execução.", e);
        }
    }
}
