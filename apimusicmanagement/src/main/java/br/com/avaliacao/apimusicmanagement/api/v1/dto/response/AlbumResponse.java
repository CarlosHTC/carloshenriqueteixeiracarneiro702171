package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record AlbumResponse(
    Long id,
    String nome,
    ArtistaSummaryResponse artista,
    Set<GeneroSummaryResponse> generos,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
