package br.com.avaliacao.apimusicmanagement.config.integration.regionais.client;

import br.com.avaliacao.apimusicmanagement.config.integration.regionais.dto.RegionalExternalDTO;

import java.util.List;

public interface RegionalExternalClient {
    List<RegionalExternalDTO> listarRegionais();
}
