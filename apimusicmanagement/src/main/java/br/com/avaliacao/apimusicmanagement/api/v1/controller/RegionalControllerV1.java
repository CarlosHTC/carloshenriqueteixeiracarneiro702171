package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.RegionalResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.mapper.RegionalMapper;
import br.com.avaliacao.apimusicmanagement.domain.repository.RegionalRepository;
import br.com.avaliacao.apimusicmanagement.domain.service.RegionalSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/regionais")
public class RegionalControllerV1 {

    private final RegionalRepository regionalRepository;
    private final RegionalSyncService regionalSyncService;

    public RegionalControllerV1(RegionalRepository regionalRepository, RegionalSyncService regionalSyncService) {
        this.regionalRepository = regionalRepository;
        this.regionalSyncService = regionalSyncService;
    }

    @GetMapping
    @Operation(summary = "Listar regionais", description = "Lista regionais por status")
    public List<RegionalResponse> listar(@RequestParam(defaultValue = "true") boolean ativo) {
        return regionalRepository.findByAtivo(ativo).stream()
                .sorted(Comparator.comparing(regional -> regional.getNome() == null ? "" : regional.getNome()))
                .map(RegionalMapper::toResponse)
                .toList();
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Sincronizar regionais", description = "Força a sincronização com o endpoint externo.")
    public void sync() {
        regionalSyncService.sync();
    }
}
