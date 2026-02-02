package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.ArtistaFotoResponse;
import br.com.avaliacao.apimusicmanagement.domain.exception.ResourceNotFoundException;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Artista;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.ArtistaFoto;
import br.com.avaliacao.apimusicmanagement.domain.repository.ArtistaFotoRepository;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.ArtistaFotoStorage;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.MinioProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.UUID;

@Service
public class ArtistaFotoService {

    private final ArtistaService artistaService;
    private final ArtistaFotoRepository artistaFotoRepository;
    private final ArtistaFotoStorage artistaFotoStorage;
    private final MinioProperties minioProperties;

    public ArtistaFotoService(
            ArtistaService artistaService,
            ArtistaFotoRepository artistaFotoRepository,
            ArtistaFotoStorage artistaFotoStorage,
            MinioProperties minioProperties) {
        this.artistaService = artistaService;
        this.artistaFotoRepository = artistaFotoRepository;
        this.artistaFotoStorage = artistaFotoStorage;
        this.minioProperties = minioProperties;
    }

    @Transactional
    public ArtistaFotoResponse upload(Long artistaId, MultipartFile file) {
        Artista artista = artistaService.buscarPorId(artistaId);
        validarImagem(file);

        String objectKey = gerarObjectKey(artistaId, file.getOriginalFilename());
        artistaFotoStorage.upload(objectKey, file);

        ArtistaFoto existente = artistaFotoRepository.findByArtistaId(artistaId).orElse(null);
        String oldObjectKey = existente != null ? existente.getObjectKey() : null;

        try {
            ArtistaFoto foto = existente != null ? existente : new ArtistaFoto();
            foto.setArtista(artista);
            foto.setBucket(minioProperties.getArtistBucket());
            foto.setObjectKey(objectKey);
            foto.setContentType(safeName(file.getContentType()));
            foto.setSizeBytes(file.getSize());

            ArtistaFoto saved = artistaFotoRepository.save(foto);

            Duration duration = Duration.ofMinutes(minioProperties.getPresignExpirationMinutes());
            String url = artistaFotoStorage.presignGetUrl(saved.getObjectKey(), duration);

            if (oldObjectKey != null && !oldObjectKey.equals(saved.getObjectKey())) {
                artistaFotoStorage.delete(oldObjectKey);
            }

            return toResponse(saved, url);
        } catch (RuntimeException ex) {
            artistaFotoStorage.delete(objectKey);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public ArtistaFotoResponse buscar(Long artistaId) {
        ArtistaFoto foto = artistaFotoRepository.findByArtistaId(artistaId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto do artista nao encontrada: artistaId=" + artistaId));

        Duration duration = Duration.ofMinutes(minioProperties.getPresignExpirationMinutes());
        String url = artistaFotoStorage.presignGetUrl(foto.getObjectKey(), duration);
        return toResponse(foto, url);
    }

    @Transactional
    public void remover(Long artistaId) {
        ArtistaFoto foto = artistaFotoRepository.findByArtistaId(artistaId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto do artista nao encontrada: artistaId=" + artistaId));
        artistaFotoRepository.delete(foto);
        artistaFotoStorage.delete(foto.getObjectKey());
    }

    private ArtistaFotoResponse toResponse(ArtistaFoto foto, String url) {
        return new ArtistaFotoResponse(
                foto.getId(),
                foto.getContentType(),
                foto.getSizeBytes(),
                url
        );
    }

    private void validarImagem(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo invalido (vazio).");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas arquivos de imagem sao permitidos.");
        }
    }

    private String gerarObjectKey(Long artistaId, String originalName) {
        String ext = extrairExtensao(originalName);
        String uuid = UUID.randomUUID().toString();
        return "artistas/" + artistaId + "/" + uuid + (ext.isBlank() ? "" : "." + ext);
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
