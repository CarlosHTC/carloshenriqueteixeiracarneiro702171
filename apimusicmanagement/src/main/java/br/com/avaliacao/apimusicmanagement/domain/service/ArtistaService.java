package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.ArtistaListResponse;
import br.com.avaliacao.apimusicmanagement.domain.exception.ResourceNotFoundException;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Artista;
import br.com.avaliacao.apimusicmanagement.domain.model.enums.TipoArtista;
import br.com.avaliacao.apimusicmanagement.domain.repository.ArtistaRepository;
import br.com.avaliacao.apimusicmanagement.domain.repository.projection.ArtistaListProjection;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.ArtistaFotoStorage;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.MinioProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.function.Consumer;

@Service
public class ArtistaService {

    private final ArtistaRepository artistaRepository;
    private final MinioProperties minioProperties;
    private final ArtistaFotoStorage artistaFotoStorage;

    public ArtistaService(ArtistaRepository artistaRepository, MinioProperties minioProperties, ArtistaFotoStorage artistaFotoStorage) {
        this.artistaRepository = artistaRepository;
        this.minioProperties = minioProperties;
        this.artistaFotoStorage = artistaFotoStorage;
    }

//    @Transactional(readOnly = true)
//    public Page<Artista> listar(String nome, Pageable pageable) {
//        if (nome == null || nome.isBlank()) {
//            return artistaRepository.findAll(pageable);
//        }
//        return artistaRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
//    }

    @Transactional
    public Page<ArtistaListResponse> listar(String nome, TipoArtista tipo, Long regionalId, Pageable pageable) {
        Page<ArtistaListProjection> page = (regionalId != null || tipo != null || nome != null)
                ? artistaRepository.listarPaginadoFiltrado(tipo, nome != null ? nome : "", regionalId, pageable)
                : artistaRepository.listarPaginado(pageable);

        return page.map(artistaListProjection -> new ArtistaListResponse(
                artistaListProjection.getId(),
                artistaListProjection.getNome(),
                artistaListProjection.getTipo(),
                artistaListProjection.getQtdAlbuns() == null ? 0L : artistaListProjection.getQtdAlbuns(),
                buildFotoUrl(artistaListProjection.getFotoObjectKey())
        ));
    }

    @Transactional(readOnly = true)
    public Artista buscarPorId(Long id) {
        return artistaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado: id=" + id));
    }

    @Transactional
    public Artista criar (Artista artista) {
        return artistaRepository.save(artista);
    }

    @Transactional
    public Artista atualizar (Long id, Consumer<Artista> updater) {
        Artista existente = buscarPorId(id);
        updater.accept(existente);
        return artistaRepository.save(existente);
    }

    @Transactional
    public void deletar (Long id) {
        Artista existente = buscarPorId(id);
        artistaRepository.delete(existente);
    }

    private String buildFotoUrl(String objectKey) {
        if (minioProperties.getArtistBucket() == null || minioProperties.getArtistBucket().isBlank() || objectKey == null) return null;

        Duration duration = Duration.ofMinutes(minioProperties.getPresignExpirationMinutes());

        return artistaFotoStorage.presignGetUrl(objectKey, duration);
    }

}
