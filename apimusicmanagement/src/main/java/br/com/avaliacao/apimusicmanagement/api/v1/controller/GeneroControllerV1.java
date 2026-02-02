package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.GeneroCreateRquest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.GeneroUpdateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.GeneroResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.mapper.GeneroMapper;
import br.com.avaliacao.apimusicmanagement.domain.service.GeneroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/generos")
public class GeneroControllerV1 {

    private final GeneroService generoService;
    private final GeneroMapper generoMapper;

    public GeneroControllerV1(GeneroService generoService, GeneroMapper generoMapper) {
        this.generoService = generoService;
        this.generoMapper = generoMapper;
    }

    @GetMapping
    public List<GeneroResponse> listar() {
        return generoService.listar().stream().map(generoMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public GeneroResponse buscar(@PathVariable Long id) {
        return generoMapper.toResponse(generoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<GeneroResponse> criar(@RequestBody @Valid GeneroCreateRquest request) {
        var entity = generoMapper.toEntity(request);
        var salvo = generoService.criar(entity);
        var uri = URI.create("/api/v1/generos/" + entity.getId());
        return ResponseEntity.created(uri).body(generoMapper.toResponse(salvo));
    }

    @PutMapping("/{id}")
    public GeneroResponse atualizar(@PathVariable Long id, @RequestBody @Valid GeneroUpdateRequest request) {
        var atualizado = generoService.atualizar(id, entity -> generoMapper.updateEntity(request, entity));
        return generoMapper.toResponse(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        generoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
