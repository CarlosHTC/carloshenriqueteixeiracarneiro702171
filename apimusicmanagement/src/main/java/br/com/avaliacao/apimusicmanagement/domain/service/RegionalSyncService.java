package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.config.integration.regionais.client.RegionalExternalClient;
import br.com.avaliacao.apimusicmanagement.config.integration.regionais.dto.RegionalExternalDTO;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Regional;
import br.com.avaliacao.apimusicmanagement.domain.repository.RegionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RegionalSyncService {

    private final RegionalRepository regionalRepository;
    private final RegionalExternalClient regionalExternalClient;

    public RegionalSyncService(RegionalRepository regionalRepository, RegionalExternalClient regionalExternalClient) {
        this.regionalRepository = regionalRepository;
        this.regionalExternalClient = regionalExternalClient;
    }

    @Transactional
    public void sync() {
        List<RegionalExternalDTO> remotas = Optional.ofNullable(regionalExternalClient.listarRegionais())
                .orElseGet(List::of);

        Instant now = Instant.now();

        Map<Long, Regional> ativasPorExternalId = regionalRepository.findByAtivoTrue().stream()
                .collect(Collectors.toMap(Regional::getExternalId, Function.identity(), (a, b) -> a));

        Set<Long> externalIdsRecebidas = new HashSet<>();

        for (RegionalExternalDTO dto : remotas) {
            if (dto == null || dto.id() == null) continue;

            Long externalId = dto.id();
            String nome = dto.nome() == null ? "" : dto.nome().trim();

            externalIdsRecebidas.add(externalId);

            Regional atual = ativasPorExternalId.get(externalId);
            if (atual == null) {
                regionalRepository.save(Regional.novaAtiva(externalId, nome, now));
                continue;
            }

            String nomeAtual = atual.getNome() == null ? "" : atual.getNome().trim();
            if (!nomeAtual.equals(nome)) {
                regionalRepository.inativar(atual.getId(), now);
                regionalRepository.save(Regional.novaAtiva(externalId, nome, now));
            }
        }

        for (Regional ativa : ativasPorExternalId.values()) {
            if (!externalIdsRecebidas.contains(ativa.getExternalId())) {
                regionalRepository.inativar(ativa.getId(), now);
            }
        }
    }
}
