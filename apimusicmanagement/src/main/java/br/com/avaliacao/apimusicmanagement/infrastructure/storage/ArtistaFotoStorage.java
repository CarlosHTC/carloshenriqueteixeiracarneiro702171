package br.com.avaliacao.apimusicmanagement.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public interface ArtistaFotoStorage {
    void upload(String objectKey, MultipartFile file);
    String presignGetUrl(String objectKey, Duration duration);
    void delete(String objectKey);
}
