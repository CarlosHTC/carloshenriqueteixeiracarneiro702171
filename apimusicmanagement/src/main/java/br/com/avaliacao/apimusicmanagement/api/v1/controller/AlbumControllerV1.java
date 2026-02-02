package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.AlbumCreateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.AlbumUpdateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumListResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.mapper.AlbumMapper;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import br.com.avaliacao.apimusicmanagement.domain.service.AlbumService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/albuns")
public class AlbumControllerV1 {

    private final AlbumService albumService;
    private final AlbumMapper albumMapper;

    public AlbumControllerV1(AlbumService albumService, AlbumMapper albumMapper) {
        this.albumService = albumService;
        this.albumMapper = albumMapper;
    }

    @GetMapping
    public Page<AlbumListResponse> listar(@RequestParam(required = false) String nome, Pageable pageable) {
        return albumService.listar(nome, pageable);
    }

    @GetMapping("/artista")
    public Page<AlbumListResponse> listarPorArtista(@RequestParam Long artistaId, Pageable pageable) {
        return albumService.listarPorArtista(artistaId, pageable);
    }

    @GetMapping("/{id}")
    public AlbumResponse buscar(@PathVariable Long id) {
        return albumMapper.toResponse(albumService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AlbumResponse> criar(@RequestBody @Valid AlbumCreateRequest request) {
        var salvo = albumService.criar(request.nome(), request.artistaId(), request.generoIds());
        var uri = URI.create("/api/v1/albuns" + salvo.getId());
        return ResponseEntity.created(uri).body(albumMapper.toResponse(salvo));
    }

    @PutMapping("/{id}")
    public AlbumResponse atualizar(@PathVariable Long id, @RequestBody @Valid AlbumUpdateRequest request) {
        var atualizado = albumService.atualizar(id, request.nome(), request.artistaId(), request.generoIds());
        return albumMapper.toResponse(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        albumService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
