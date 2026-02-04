package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumCapaResponse;
import br.com.avaliacao.apimusicmanagement.domain.service.AlbumCapaService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albuns/{albumId}/capas")
public class AlbumCapaControllerV1 {

    private final AlbumCapaService albumCapaService;

    public AlbumCapaControllerV1(AlbumCapaService albumCapaService) {
        this.albumCapaService = albumCapaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<AlbumCapaResponse>> upload(
            @PathVariable Long albumId,
            @RequestPart("files") @NotNull List<MultipartFile> files
    ) {
        return ResponseEntity.ok(albumCapaService.upload(albumId, files));
    }

    @GetMapping
    public ResponseEntity<List<AlbumCapaResponse>> listar(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumCapaService.listar(albumId));
    }

    @DeleteMapping("/{capaId}")
    public ResponseEntity<Void> remover(@PathVariable Long albumId, @PathVariable Long capaId) {
        albumCapaService.remover(albumId, capaId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{capaId}/principal")
    public ResponseEntity<Void> definirPrincipal(@PathVariable Long albumId, @PathVariable Long capaId) {
        albumCapaService.definirPrincipal(albumId, capaId);
        return ResponseEntity.noContent().build();
    }
}
