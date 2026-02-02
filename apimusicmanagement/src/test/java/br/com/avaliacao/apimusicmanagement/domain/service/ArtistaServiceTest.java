package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.domain.repository.ArtistaRepository;
import br.com.avaliacao.apimusicmanagement.domain.repository.projection.ArtistaListProjection;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.ArtistaFotoStorage;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.MinioProperties;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.*;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistaServiceTest {

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private MinioProperties minioProperties;

    @Mock
    private ArtistaFotoStorage artistaFotoStorage;

    @InjectMocks
    private ArtistaService artistaService;

    @Test
    void listar_semRegional_deveUsarListarPaginado() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("nome").ascending());
        ArtistaListProjection projection = mock(ArtistaListProjection.class);
        when(projection.getId()).thenReturn(1L);
        when(projection.getNome()).thenReturn("A");
        when(projection.getTipo()).thenReturn("BANDA");
        when(projection.getQtdAlbuns()).thenReturn(2L);
        when(projection.getFotoObjectKey()).thenReturn("foto-a");

        Page<ArtistaListProjection> page = new PageImpl<>(List.of(projection), pageable, 1);

        when(artistaRepository.listarPaginado( pageable)).thenReturn(page);
        when(minioProperties.getArtistBucket()).thenReturn("bucket-artistas");
        when(minioProperties.getPresignExpirationMinutes()).thenReturn(10);
        when(artistaFotoStorage.presignGetUrl(eq("foto-a"), eq(Duration.ofMinutes(10))))
                .thenReturn("http://minio/foto-a");

        Page<br.com.avaliacao.apimusicmanagement.api.v1.dto.response.ArtistaListResponse> result =
                artistaService.listar(null, null,null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).fotoUrl()).isEqualTo("http://minio/foto-a");
        verify(artistaRepository).listarPaginado(pageable);
        verify(artistaRepository, never()).listarPaginadoFiltrado(any(), any(), any(), any());
    }

    @Test
    void listar_comRegional_deveUsarListarPaginadoComFiltroRegionalIncluiSemRegional() {
        Pageable pageable = PageRequest.of(0, 10);
        ArtistaListProjection projection = mock(ArtistaListProjection.class);
        when(projection.getId()).thenReturn(2L);
        when(projection.getNome()).thenReturn("Mike");
        when(projection.getTipo()).thenReturn("SOLO");
        when(projection.getQtdAlbuns()).thenReturn(null);
        when(projection.getFotoObjectKey()).thenReturn("");

        Page<ArtistaListProjection> page = new PageImpl<>(List.of(projection), pageable, 1);

        when(artistaRepository.listarPaginadoFiltrado(null,"mike", 10L, pageable))
                .thenReturn(page);
        when(minioProperties.getArtistBucket()).thenReturn("bucket-artistas");

        Page<br.com.avaliacao.apimusicmanagement.api.v1.dto.response.ArtistaListResponse> result =
                artistaService.listar("mike",null, 10L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).qtdAlbuns()).isEqualTo(0L);
        assertThat(result.getContent().get(0).fotoUrl()).isNull();
        verify(artistaRepository).listarPaginadoFiltrado(null,"mike", 10L, pageable);
        verify(artistaRepository, never()).listarPaginado(any());
    }

}
