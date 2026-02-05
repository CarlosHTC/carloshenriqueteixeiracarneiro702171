package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record AlbumListResponse(
    Long id,
    String nome,
    ArtistaSummaryResponse artista,
    Set<GeneroSummaryResponse> generos,
    List<AlbumCapaResponse> capas,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
