package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumCapaResponse;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.AlbumCapa;
import br.com.avaliacao.apimusicmanagement.domain.repository.AlbumCapaRepository;
import br.com.avaliacao.apimusicmanagement.domain.repository.AlbumRepository;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.AlbumCapaStorage;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.MinioProperties;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AlbumCapaService {

    private final AlbumRepository albumRepository;
    private final AlbumCapaRepository albumCapaRepository;
    private final AlbumCapaStorage albumCapaStorage;
    private final MinioProperties minioProperties;

    public AlbumCapaService(AlbumRepository albumRepository, AlbumCapaRepository albumCapaRepository, AlbumCapaStorage albumCapaStorage, MinioProperties minioProperties) {
        this.albumRepository = albumRepository;
        this.albumCapaRepository = albumCapaRepository;
        this.albumCapaStorage = albumCapaStorage;
        this.minioProperties = minioProperties;
    }

    @Transactional
    public List<AlbumCapaResponse> upload(Long albumId, List<MultipartFile> files) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album não encontrado: " + albumId));

        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<AlbumCapaResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            validarImagem(file);

            String objectKey = gerarObjectKey(albumId, file.getOriginalFilename());
            albumCapaStorage.upload(objectKey, file);

            try {
                AlbumCapa capa = new AlbumCapa(
                        objectKey,
                        safeName(file.getOriginalFilename()),
                        safeName(file.getContentType()),
                        file.getSize()
                );
                album.adicionarCapa(capa);

                AlbumCapa saved = albumCapaRepository.save(capa);

                Duration duration = Duration.ofMinutes(minioProperties.getPresignExpirationMinutes());

                String url = albumCapaStorage.presignGetUrl(saved.getObjectKey(), duration);
                responses.add(toResponse(saved, url));

            } catch (RuntimeException ex) {
                albumCapaStorage.delete(objectKey);
                throw ex;
            }
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<AlbumCapaResponse> listar(Long albumId) {
        if (!albumRepository.existsById(albumId)) {
            throw new EntityNotFoundException("Album não encontrado: " + albumId);
        }

        Duration duration = Duration.ofMinutes(minioProperties.getPresignExpirationMinutes());

        return albumCapaRepository.findByAlbumIdOrderByCreatedAtDesc(albumId).stream()
                .map(c -> toResponse(c, albumCapaStorage.presignGetUrl(c.getObjectKey(), duration)))
                .toList();
    }

    @Transactional
    public void remover(Long albumId, Long capaId) {
        AlbumCapa capa = albumCapaRepository.findByIdAndAlbumId(capaId, albumId)
                .orElseThrow(() -> new EntityNotFoundException("Capa não encontrada: " + capaId + " (album " + albumId + ")"));

        albumCapaRepository.delete(capa);
        albumCapaStorage.delete(capa.getObjectKey());
    }

    private AlbumCapaResponse toResponse(AlbumCapa capa, String url) {
        return new AlbumCapaResponse(
                capa.getId(),
                capa.getFileName(),
                capa.getContentType(),
                capa.getSizeBytes(),
                capa.isPrincipal(),
                url
        );
    }

    private void validarImagem(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo inválido (vazio).");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas arquivos de imagem são permitidos.");
        }
    }

    private String gerarObjectKey(Long albumId, String originalName) {
        String ext = extrairExtensao(originalName);
        String uuid = UUID.randomUUID().toString();
        return "albums/" + albumId + "/" + uuid + (ext.isBlank() ? "" : "." + ext);
    }

    private String extrairExtensao(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) return "";
        return name.substring(idx + 1).toLowerCase();
    }

    private String safeName(String v) {
        return v == null ? "" : v;
    }
}
