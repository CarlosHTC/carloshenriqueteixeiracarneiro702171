package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.AlbumCapa;
import br.com.avaliacao.apimusicmanagement.domain.repository.AlbumCapaRepository;
import br.com.avaliacao.apimusicmanagement.domain.repository.AlbumRepository;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.AlbumCapaStorage;
import br.com.avaliacao.apimusicmanagement.infrastructure.storage.MinioProperties;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumCapaServiceTest {

    @Mock
    AlbumRepository albumRepository;

    @Mock
    AlbumCapaRepository albumCapaRepository;

    @Mock
    AlbumCapaStorage albumCapaStorage;

    @Mock
    MinioProperties minioProperties;

    @InjectMocks
    AlbumCapaService albumCapaService;

    @Test
    void upload_devePersistirCapa_eRetornarPresignedUrl() {
        when(minioProperties.getPresignExpirationMinutes()).thenReturn(30);
        Long albumId = 10L;

        Album album = mock(Album.class);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        when(albumCapaRepository.save(any(AlbumCapa.class))).thenAnswer(inv -> {
            AlbumCapa capa = inv.getArgument(0);
            var f = AlbumCapa.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(capa, 99L);
            return capa;
        });

        when(albumCapaStorage.presignGetUrl(anyString(), any())).thenReturn("http://presigned.example/img");

        MockMultipartFile file = new MockMultipartFile(
                "files", "capa.jpg", "image/jpeg", "abc".getBytes()
        );

        List<?> resp = albumCapaService.upload(albumId, List.of(file));

        verify(albumCapaStorage, times(1)).upload(anyString(), eq(file));
        verify(albumCapaRepository, times(1)).save(any(AlbumCapa.class));
        verify(albumCapaStorage, times(1)).presignGetUrl(anyString(), any());

        assertThat(resp).hasSize(1);
    }

    @Test
    void listar_quandoAlbumNaoExiste_deveLancar404Dominio() {
        when(albumRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> albumCapaService.listar(10L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void remover_deveExcluirRegistro_eObjeto() {
        Long albumId = 1L;
        Long capaId = 2L;

        AlbumCapa capa = mock(AlbumCapa.class);
        when(capa.getObjectKey()).thenReturn("albums/1/x.jpg");
        when(albumCapaRepository.findByIdAndAlbumId(capaId, albumId)).thenReturn(Optional.of(capa));

        albumCapaService.remover(albumId, capaId);

        verify(albumCapaRepository).delete(capa);
        verify(albumCapaStorage).delete("albums/1/x.jpg");
    }
}