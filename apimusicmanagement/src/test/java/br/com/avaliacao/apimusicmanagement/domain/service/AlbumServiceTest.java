package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumCapaResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumListResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.mapper.AlbumMapper;
import br.com.avaliacao.apimusicmanagement.domain.exception.ResourceNotFoundException;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.AlbumCapa;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Artista;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Genero;
import br.com.avaliacao.apimusicmanagement.domain.model.enums.TipoArtista;
import br.com.avaliacao.apimusicmanagement.domain.repository.AlbumCapaRepository;
import br.com.avaliacao.apimusicmanagement.domain.repository.AlbumRepository;
import br.com.avaliacao.apimusicmanagement.domain.repository.GeneroRepository;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.AlbumCapaStorage;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.MinioProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock AlbumRepository albumRepository;
    @Mock ArtistaService artistaService;
    @Mock GeneroRepository generoRepository;
    @Mock AlbumCapaRepository albumCapaRepository;
    @Mock AlbumCapaStorage albumCapaStorage;
    @Mock MinioProperties minioProperties;
    @Mock AlbumMapper albumMapper;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks AlbumService albumService;

    @Test
    void listar_deveRetornarCapasPorAlbum() {
        Pageable pageable = PageRequest.of(0, 10);

        Album album = new Album("Harakiri");
        ReflectionTestUtils.setField(album, "id", 10L);

        when(albumRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(album), pageable, 1));

        AlbumCapa capa = new AlbumCapa(
                "albuns/10/capa.png",
                "capa.png",
                "image/png",
                123L
        );
        capa.setAlbum(album);

        ReflectionTestUtils.setField(capa, "id", 99L);

        when(albumCapaRepository.findByAlbumIdInOrderByUpdatedAtDesc(Set.of(10L)))
                .thenReturn(List.of(capa));

        when(minioProperties.getPresignExpirationMinutes()).thenReturn(30);
        when(albumCapaStorage.presignGetUrl(eq("albuns/10/capa.png"), any()))
                .thenReturn("http://presigned");

        AlbumListResponse mapped = mock(AlbumListResponse.class);
        when(albumMapper.toListResponse(eq(album), anyList()))
                .thenReturn(mapped);

        Page<AlbumListResponse> result = albumService.listar(null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(albumCapaRepository).findByAlbumIdInOrderByUpdatedAtDesc(Set.of(10L));
        verify(albumMapper).toListResponse(eq(album),
                argThat((List<AlbumCapaResponse> capas) ->
                        capas != null && !capas.isEmpty() && "http://presigned".equals(capas.get(0).url())
                ));
    }

    @Test
    void listar_comNome_deveUsarRepositorioFiltrado() {
        Pageable pageable = PageRequest.of(0, 10);
        when(albumRepository.findByNomeContainingIgnoreCase("post", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<AlbumListResponse> result = albumService.listar("post", pageable);

        assertThat(result.getContent()).isEmpty();
        verify(albumRepository).findByNomeContainingIgnoreCase("post", pageable);
        verify(albumRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void buscarPorId_deveRetornarAlbum_quandoExiste() {
        Album album = new Album("Nevermind");
        when(albumRepository.findById(10L)).thenReturn(Optional.of(album));

        var resposta = albumService.buscarPorId(10L);

        assertEquals("Nevermind", resposta.getNome());
    }

    @Test
    void buscarPorId_deveLancar404_quandoNaoExiste() {
        when(albumRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> albumService.buscarPorId(999L));
    }

    @Test
    void criar_devePersistirAlbumComArtistaEGeneros() {
        var artista = new Artista("Nirvana", TipoArtista.BANDA);
        when(artistaService.buscarPorId(1L)).thenReturn(artista);

        var genero1 = new Genero("Rock");
        var genero2 = new Genero("Grunge");
        Set<Long> generoIds = Set.of(10L, 11L);
        when(generoRepository.findAllById(generoIds)).thenReturn(List.of(genero1, genero2));

        when(albumRepository.save(any(Album.class))).thenAnswer(inv -> inv.getArgument(0));

        Album salvo = albumService.criar("Nevermind", 1L, generoIds);

        assertEquals("Nevermind", salvo.getNome());
        assertNotNull(salvo.getArtista());
        assertEquals(2, salvo.getGeneros().size());
        verify(albumRepository).save(any(Album.class));
    }

    @Test
    void criar_deveLancar404_quandoGeneroIdInexistente() {
        var artista = new Artista("Nirvana", TipoArtista.BANDA);
        when(artistaService.buscarPorId(1L)).thenReturn(artista);

        Set<Long> generoIds = Set.of(10L, 11L);
        when(generoRepository.findAllById(generoIds)).thenReturn(List.of(new Genero("Rock")));

        assertThrows(ResourceNotFoundException.class, () ->
                albumService.criar("Nevermind", 1L, generoIds)
        );

        verify(albumRepository, never()).save(any());
    }
}
