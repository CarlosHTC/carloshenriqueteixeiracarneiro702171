package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.ArtistaFotoResponse;
import br.com.avaliacao.apimusicmanagement.domain.service.ArtistaFotoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/artistas/{artistaId}/foto")
public class ArtistaFotoControllerV1 {

    private final ArtistaFotoService artistaFotoService;

    public ArtistaFotoControllerV1(ArtistaFotoService artistaFotoService) {
        this.artistaFotoService = artistaFotoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtistaFotoResponse> upload(
            @PathVariable Long artistaId,
            @RequestPart("file") @NotNull MultipartFile file
    ) {
        return ResponseEntity.ok(artistaFotoService.upload(artistaId, file));
    }

    @GetMapping
    public ArtistaFotoResponse buscar(@PathVariable Long artistaId) {
        return artistaFotoService.buscar(artistaId);
    }

    @DeleteMapping
    public ResponseEntity<Void> remover(@PathVariable Long artistaId) {
        artistaFotoService.remover(artistaId);
        return ResponseEntity.noContent().build();
    }
}
