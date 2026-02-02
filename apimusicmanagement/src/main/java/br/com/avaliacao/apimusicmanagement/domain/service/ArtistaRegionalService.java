package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.domain.model.entity.Artista;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Regional;
import br.com.avaliacao.apimusicmanagement.domain.repository.ArtistaRepository;
import br.com.avaliacao.apimusicmanagement.domain.repository.RegionalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ArtistaRegionalService {

    private final ArtistaRepository artistaRepository;
    private final RegionalRepository regionalRepository;

    public ArtistaRegionalService(ArtistaRepository artistaRepository, RegionalRepository regionalRepository) {
        this.artistaRepository = artistaRepository;
        this.regionalRepository = regionalRepository;
    }

    @Transactional
    public void atualizarRegionais(Long artistaId, Set<Long> regionalIds) {
        Artista artista = artistaRepository.findById(artistaId)
                .orElseThrow(() -> new EntityNotFoundException("Artista não encontrado: " + artistaId));

        if (regionalIds == null || regionalIds.isEmpty()) {
            artista.getRegionais().clear();
            return;
        }

        List<Regional> regionais = regionalRepository.findAllById(regionalIds);
        if (regionais.size() != regionalIds.size()){
            throw new IllegalArgumentException("Uma ou mais regionais informadas não existem.");
        }
        boolean inativa = regionais.stream().anyMatch(regional -> !Boolean.TRUE.equals(regional.getAtivo()));
        if (inativa) {
            throw new IllegalArgumentException("Não é permitido vincular artista a regional inativa.");
        }

        artista.getRegionais().clear();
        artista.getRegionais().addAll(regionais);
    }
}
