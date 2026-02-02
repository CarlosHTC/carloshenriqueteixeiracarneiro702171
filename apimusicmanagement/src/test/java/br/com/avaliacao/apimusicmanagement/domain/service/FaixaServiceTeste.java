package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.FaixaCreateRequest;
import br.com.avaliacao.apimusicmanagement.domain.exception.ConflictException;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Faixa;
import br.com.avaliacao.apimusicmanagement.domain.repository.FaixaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FaixaServiceTeste {

    @Mock
    FaixaRepository faixaRepository;

    @Mock
    AlbumService albumService;

    @InjectMocks
    FaixaService faixaService;

    @Test
    void listarPorAlbum_deveConsultarRepositorioOrdenado() {
        when(albumService.buscarPorId(1L)).thenReturn(mock(Album.class));
        when(faixaRepository.findByAlbumIdOrderByNumeroAsc(1L)).thenReturn(List.of());

        var response = faixaService.listarPorAlbum(1L);

        assertNotNull(response);
        verify(faixaRepository).findByAlbumIdOrderByNumeroAsc(1L);
    }

    @Test
    void criar_deveLancar409_quando_DuplicadoNoAlbum() {
        when(albumService.buscarPorId(1L)).thenReturn(mock(Album.class));
        when(faixaRepository.existsByAlbumIdAndNumero(1L, 1)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                faixaService.criar(1L, new FaixaCreateRequest(1, "Intro", 120, false)));

        verify(faixaRepository, never()).save(any());
    }

    @Test
    void buscarPorId_deveRetornarQuandoPertenceAoAlbum() {
        Faixa faixa = new Faixa(1, "Intro", 120, false);
        when(faixaRepository.findByIdAndAlbumId(10L, 1L)).thenReturn(Optional.of(faixa));

        Faixa resposta = faixaService.buscarPorId(1L, 10L);

        assertEquals("Intro", resposta.getTitulo());
    }
}
