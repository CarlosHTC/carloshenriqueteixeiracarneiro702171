package br.com.avaliacao.apimusicmanagement.infrastructure.websocket;

import br.com.avaliacao.apimusicmanagement.domain.event.AlbumCreatedEvent;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Artista;
import br.com.avaliacao.apimusicmanagement.domain.repository.AlbumRepository;
import org.junit.jupiter.api.Test;

import org.assertj.core.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AlbumWebSocketNotifierTest {

    @Test
    void onAlbumCreated_deveEnviarNotificacaoNoTopico() {
        AlbumRepository albumRepository = mock(AlbumRepository.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);

        AlbumWebSocketNotifier notifier = new AlbumWebSocketNotifier(albumRepository, messagingTemplate);

        Artista artista = mock(Artista.class);
        when(artista.getId()).thenReturn(7L);
        when(artista.getNome()).thenReturn("Mike Shinoda");

        Album album = mock(Album.class);
        when(album.getId()).thenReturn(10L);
        when(album.getNome()).thenReturn("Post Traumatic");
        when(album.getArtista()).thenReturn(artista);

        when(album.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(albumRepository.findById(10L)).thenReturn(Optional.of(album));
        notifier.onAlbumCreated(new AlbumCreatedEvent(10L));

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        verify(messagingTemplate, times(1))
                .convertAndSend(eq(AlbumWebSocketNotifier.DESTINATION), payloadCaptor.capture());

        Object payload = payloadCaptor.getValue();
        assertThat(payload).isNotNull();

        assertThat(payload.toString()).contains("id=10");
        assertThat(payload.toString()).contains("nome=Post Traumatic");
    }

    @Test
    void onAlbumCreated_quandoAlbumNaoExiste_naoDeveEnviar() {
        AlbumRepository albumRepository = mock(AlbumRepository.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);

        AlbumWebSocketNotifier notifier = new AlbumWebSocketNotifier(albumRepository, messagingTemplate);

        when(albumRepository.findById(10L)).thenReturn(Optional.empty());

        notifier.onAlbumCreated(new AlbumCreatedEvent(10L));

        verifyNoInteractions(messagingTemplate);
    }
}
