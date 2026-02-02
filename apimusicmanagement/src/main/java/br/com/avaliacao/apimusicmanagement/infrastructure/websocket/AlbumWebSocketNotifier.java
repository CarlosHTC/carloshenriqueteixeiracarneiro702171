package br.com.avaliacao.apimusicmanagement.infrastructure.websocket;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.GeneroSummaryResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.ws.AlbumCreatedWsMessage;
import br.com.avaliacao.apimusicmanagement.domain.event.AlbumCreatedEvent;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import br.com.avaliacao.apimusicmanagement.domain.repository.AlbumRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AlbumWebSocketNotifier {

    public static final String DESTINATION = "/topic/albuns";

    private final AlbumRepository albumRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AlbumWebSocketNotifier(AlbumRepository albumRepository, SimpMessagingTemplate messagingTemplate) {
        this.albumRepository = albumRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void onAlbumCreated(AlbumCreatedEvent createdEvent) {
        Album album = albumRepository.findById(createdEvent.albumId())
                .orElse(null);

        if (album == null) {
            return;
        }

        var artista = album.getArtista();

        Set<GeneroSummaryResponse> generos = album.getGeneros().stream()
                .map(g -> new GeneroSummaryResponse(g.getId(), g.getNome()))
                .collect(Collectors.toUnmodifiableSet());

        AlbumCreatedWsMessage payload = new AlbumCreatedWsMessage(
                album.getId(),
                album.getNome(),
                artista.getId(),
                generos,
                album.getCreatedAt(),
                album.getUpdatedAt()
        );

        messagingTemplate.convertAndSend(DESTINATION, payload);
    }
}
