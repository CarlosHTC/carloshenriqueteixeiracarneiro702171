package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record AlbumListResponse(
    Long id,
    String nome,
    ArtistaSummaryResponse artista,
    Set<GeneroSummaryResponse> generos,
    AlbumCapaPrincipalResponse capaPrincipal,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
