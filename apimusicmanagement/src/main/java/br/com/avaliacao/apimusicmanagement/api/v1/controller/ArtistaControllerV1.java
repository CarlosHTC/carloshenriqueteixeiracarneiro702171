package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.ArtistaCreateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.ArtistaUpdateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.ArtistaListResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.ArtistaResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.mapper.ArtistaMapper;
import br.com.avaliacao.apimusicmanagement.domain.model.enums.TipoArtista;
import br.com.avaliacao.apimusicmanagement.domain.service.ArtistaRegionalService;
import br.com.avaliacao.apimusicmanagement.domain.service.ArtistaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/artistas")
public class ArtistaControllerV1 {

    private final ArtistaService artistaService;
    private final ArtistaMapper artistaMapper;
    private final ArtistaRegionalService artistaRegionalService;

    public ArtistaControllerV1(ArtistaService artistaService, ArtistaMapper artistaMapper, ArtistaRegionalService artistaRegionalService) {
        this.artistaService = artistaService;
        this.artistaMapper = artistaMapper;
        this.artistaRegionalService = artistaRegionalService;
    }

    @GetMapping
    public Page<ArtistaListResponse> listar(@RequestParam(required = false) String nome, @RequestParam(required = false) TipoArtista tipo, @RequestParam(required = false) Long regionalId, Pageable pageable) {
        return artistaService.listar(nome, tipo, regionalId, pageable);
    }

    @GetMapping("/{id}")
    public ArtistaResponse buscar(@PathVariable Long id) {
        return artistaMapper.toResponse(artistaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ArtistaResponse> criar(@RequestBody @Valid ArtistaCreateRequest request) {
        var entity = artistaMapper.toEntity(request);
        var salvo = artistaService.criar(entity);
        artistaRegionalService.atualizarRegionais(salvo.getId(), request.regionalIds());
        var uri = URI.create("/api/v1/artistas/" + salvo.getId());
        return ResponseEntity.created(uri).body(artistaMapper.toResponse(salvo));
    }

    @PutMapping("/{id}")
    public ArtistaResponse atualizar(@PathVariable Long id, @RequestBody @Valid ArtistaUpdateRequest request) {
        var atualizado = artistaService.atualizar(id, entity -> artistaMapper.updateEntity(request, entity));

        artistaRegionalService.atualizarRegionais(id, request.regionalIds());

        return artistaMapper.toResponse(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        artistaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
