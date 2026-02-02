package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumCapaPrincipalResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumListResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.mapper.AlbumMapper;
import br.com.avaliacao.apimusicmanagement.domain.event.AlbumCreatedEvent;
import br.com.avaliacao.apimusicmanagement.domain.exception.ResourceNotFoundException;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.AlbumCapa;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Genero;
import br.com.avaliacao.apimusicmanagement.domain.repository.AlbumCapaRepository;
import br.com.avaliacao.apimusicmanagement.domain.repository.AlbumRepository;
import br.com.avaliacao.apimusicmanagement.domain.repository.GeneroRepository;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.AlbumCapaStorage;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.MinioProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaService artistaService;
    private final GeneroRepository generoRepository;
    private final AlbumCapaRepository albumCapaRepository;
    private final AlbumCapaStorage albumCapaStorage;
    private final MinioProperties minioProperties;
    private final AlbumMapper albumMapper;
    private final ApplicationEventPublisher eventPublisher;

    public AlbumService(
            AlbumRepository albumRepository,
            ArtistaService artistaService,
            GeneroRepository generoRepository,
            AlbumCapaRepository albumCapaRepository,
            AlbumCapaStorage albumCapaStorage,
            MinioProperties minioProperties,
            AlbumMapper albumMapper,
            ApplicationEventPublisher eventPublisher) {
        this.albumRepository = albumRepository;
        this.artistaService = artistaService;
        this.generoRepository = generoRepository;
        this.albumCapaRepository = albumCapaRepository;
        this.albumCapaStorage = albumCapaStorage;
        this.minioProperties = minioProperties;
        this.albumMapper = albumMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<AlbumListResponse> listar(String nome, Pageable pageable) {
        Page<Album> albuns = (nome ==null || nome.isBlank()) ? albumRepository.findAll(pageable) :
                albumRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);

        Set<Long> albumIds = albuns.getContent().stream().map(Album::getId).collect(Collectors.toSet());
        Map<Long, AlbumCapa> capaPrincipalPorAlbum = buscarUltimasCapas(albumIds);
        Duration duration = Duration.ofMinutes(minioProperties.getPresignExpirationMinutes());

        return albuns.map(album -> {
            AlbumCapa capaPrincipal = capaPrincipalPorAlbum.get(album.getId());
            AlbumCapaPrincipalResponse capaResponse = null;
            if (capaPrincipal != null) {
                String url = albumCapaStorage.presignGetUrl(capaPrincipal.getObjectKey(), duration);
                capaResponse = new AlbumCapaPrincipalResponse(
                        capaPrincipal.getId(),
                        capaPrincipal.getFileName(),
                        capaPrincipal.getContentType(),
                        capaPrincipal.getSizeBytes(),
                        url
                );
            }
            return albumMapper.toListResponse(album, capaResponse);
        });
    }

    @Transactional(readOnly = true)
    public Album buscarPorId(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado: id=" + id));
    }

    @Transactional
    public Album criar(String nome, Long artistaId, Set<Long> generoIds) {
        var artista = artistaService.buscarPorId(artistaId);

        Album album = new Album(nome);
        album.setArtista(artista);

        album.getGeneros().clear();
        album.getGeneros().addAll(resolverGeneros(generoIds));
        Album salvo = albumRepository.save(album);

        eventPublisher.publishEvent(new AlbumCreatedEvent(salvo.getId()));
        return salvo;
    }

    @Transactional
    public Album atualizar(Long id, String nome, Long artistaId, Set<Long> genereoIds) {
        Album existente = buscarPorId(id);

        existente.setNome(nome);
        existente.setArtista(artistaService.buscarPorId(artistaId));

        existente.getGeneros().clear();
        existente.getGeneros().addAll(resolverGeneros(genereoIds));

        return albumRepository.save(existente);
    }

    @Transactional
    public void deletar(Long id) {
        Album existente = buscarPorId(id);
        albumRepository.delete(existente);
    }

    private Map<Long, AlbumCapa> buscarUltimasCapas(Set<Long> albumIds) {
        if (albumIds == null || albumIds.isEmpty()) {
            return Map.of();
        }
        return albumCapaRepository.findLatestByAlbumIds(albumIds).stream()
                .collect(Collectors.toMap(capa -> capa.getAlbum().getId(), Function.identity()));
    }

    private Set<Genero> resolverGeneros(Set<Long> generoIds) {
        if (generoIds == null || generoIds.isEmpty()) return new HashSet<>();

        List<Genero> generos = generoRepository.findAllById(generoIds);

        if (generos.size() != generoIds.size()) {
            throw new ResourceNotFoundException("Um ou mais gêneros não foram encontrados.");
        }
        return new HashSet<>(generos);
    }

    public Page<AlbumListResponse> listarPorArtista(Long artistaId, Pageable pageable) {
        Page<Album> albuns = albumRepository.findByArtistaId(artistaId, pageable);

        Set<Long> albumIds = albuns.getContent().stream().map(Album::getId).collect(Collectors.toSet());
        Map<Long, AlbumCapa> capaPrincipalPorAlbum = buscarUltimasCapas(albumIds);
        Duration duration = Duration.ofMinutes(minioProperties.getPresignExpirationMinutes());

        return albuns.map(album -> {
            AlbumCapa capaPrincipal = capaPrincipalPorAlbum.get(album.getId());
            AlbumCapaPrincipalResponse capaResponse = null;
            if (capaPrincipal != null) {
                String url = albumCapaStorage.presignGetUrl(capaPrincipal.getObjectKey(), duration);
                capaResponse = new AlbumCapaPrincipalResponse(
                        capaPrincipal.getId(),
                        capaPrincipal.getFileName(),
                        capaPrincipal.getContentType(),
                        capaPrincipal.getSizeBytes(),
                        url
                );
            }
            return albumMapper.toListResponse(album, capaResponse);
        });
    }
}
