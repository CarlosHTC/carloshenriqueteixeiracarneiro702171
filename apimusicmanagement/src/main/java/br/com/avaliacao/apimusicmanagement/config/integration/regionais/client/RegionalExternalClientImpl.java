package br.com.avaliacao.apimusicmanagement.config.integration.regionais.client;

import br.com.avaliacao.apimusicmanagement.config.integration.RegionalIntegrationProperties;
import br.com.avaliacao.apimusicmanagement.config.integration.regionais.dto.RegionalExternalDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class RegionalExternalClientImpl implements RegionalExternalClient{

    private final WebClient webClient;
    private final RegionalIntegrationProperties integrationProperties;

    public RegionalExternalClientImpl(WebClient webClient, RegionalIntegrationProperties integrationProperties) {
        this.webClient = webClient;
        this.integrationProperties = integrationProperties;
    }

    @Override
    public List<RegionalExternalDTO> listarRegionais() {
        return webClient.get()
                .uri(integrationProperties.getUrl())
                .retrieve()
                .bodyToFlux(RegionalExternalDTO.class)
                .collectList()
                .block();
    }
}
