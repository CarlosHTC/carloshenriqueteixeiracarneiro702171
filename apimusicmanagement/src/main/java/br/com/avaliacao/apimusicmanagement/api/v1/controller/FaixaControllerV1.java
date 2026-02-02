package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.FaixaCreateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.FaixaUpdateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.FaixaResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.mapper.FaixaMapper;
import br.com.avaliacao.apimusicmanagement.domain.service.FaixaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/albuns/{albumId}/faixas")
public class FaixaControllerV1 {

    private final FaixaService faixaService;
    private final FaixaMapper faixaMapper;

    public FaixaControllerV1(FaixaService faixaService, FaixaMapper faixaMapper) {
        this.faixaService = faixaService;
        this.faixaMapper = faixaMapper;
    }

    @GetMapping
    public List<FaixaResponse> listarFaixas(@PathVariable Long albumId) {
        return faixaService.listarPorAlbum(albumId).stream().map(faixaMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public FaixaResponse buscarFaixa(@PathVariable Long albumId, Long id) {
        return faixaMapper.toResponse(faixaService.buscarPorId(albumId, id));
    }

    @PostMapping
    public ResponseEntity<FaixaResponse> criarFaixa(@PathVariable Long albumId, @RequestBody FaixaCreateRequest request) {
        var salvo = faixaService.criar(albumId, request);
        var uri = URI.create("/api/v1/albuns/" + albumId + "/faixas/" + salvo.getId());
        return ResponseEntity.created(uri).body(faixaMapper.toResponse(salvo));
    }

    @PutMapping("/{id}")
    public FaixaResponse atualizarFaixa(@PathVariable Long albumId, Long id, @RequestBody FaixaUpdateRequest request) {
        return faixaMapper.toResponse(faixaService.atualizar(albumId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarFaixa(@PathVariable Long albumId, Long id){
        faixaService.deletar(albumId, id);
        return ResponseEntity.noContent().build();
    }
}
