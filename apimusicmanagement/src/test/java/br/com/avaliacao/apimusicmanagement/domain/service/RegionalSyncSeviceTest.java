package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.config.integration.regionais.client.RegionalExternalClient;
import br.com.avaliacao.apimusicmanagement.config.integration.regionais.dto.RegionalExternalDTO;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Regional;
import br.com.avaliacao.apimusicmanagement.domain.repository.RegionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

public class RegionalSyncSeviceTest {

    @Mock private RegionalRepository regionalRepository;
    @Mock private RegionalExternalClient regionalExternalClient;

    @InjectMocks private RegionalSyncService regionalSyncService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveInserirNovaRegionalQuandoNaoExisteAtiva() {
        when(regionalExternalClient.listarRegionais()).thenReturn(List.of(new RegionalExternalDTO(10L, "Regional X")));
        when(regionalRepository.findByAtivoTrue()).thenReturn(List.of());

        regionalSyncService.sync();

        verify(regionalRepository).save(argThat(r ->
                r.getExternalId().equals(10L) &&
                        r.getNome().equals("Regional X") &&
                        Boolean.TRUE.equals(r.getAtivo())
        ));
        verify(regionalRepository, never()).inativar(anyLong(), any());
    }

    @Test
    void deveInativarERecriarQuandoNomeAlterar() {
        Regional ativa = Regional.novaAtiva(10L, "Antiga", Instant.now());
        try {
            var idField = Regional.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(ativa, 99L);
        } catch (Exception ignored) {}

        when(regionalExternalClient.listarRegionais()).thenReturn(List.of(new RegionalExternalDTO(10L, "Nova")));
        when(regionalRepository.findByAtivoTrue()).thenReturn(List.of(ativa));
        when(regionalRepository.inativar(eq(99L), any())).thenReturn(1);

        regionalSyncService.sync();

        verify(regionalRepository).inativar(eq(99L), any());
        verify(regionalRepository).save(argThat(r ->
                r.getExternalId().equals(10L) &&
                        r.getNome().equals("Nova") &&
                        Boolean.TRUE.equals(r.getAtivo())
        ));
    }

    @Test
    void deveInativarQuandoAusenteNoEndpoint() {
        Regional ativa = Regional.novaAtiva(10L, "Regional X", Instant.now());
        try {
            var idField = Regional.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(ativa, 99L);
        } catch (Exception ignored) {}

        when(regionalExternalClient.listarRegionais()).thenReturn(List.of()); // endpoint vazio
        when(regionalRepository.findByAtivoTrue()).thenReturn(List.of(ativa));
        when(regionalRepository.inativar(eq(99L), any())).thenReturn(1);

        regionalSyncService.sync();

        verify(regionalRepository).inativar(eq(99L), any());
        verify(regionalRepository, never()).save(any());
    }
}
