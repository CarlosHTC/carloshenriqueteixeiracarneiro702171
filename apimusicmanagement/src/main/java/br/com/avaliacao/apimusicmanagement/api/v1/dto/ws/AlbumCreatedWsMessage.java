package br.com.avaliacao.apimusicmanagement.api.v1.dto.ws;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.GeneroSummaryResponse;

import java.time.LocalDateTime;
import java.util.Set;

public record AlbumCreatedWsMessage(
        Long id,
        String nome,
        Long artistaId,
        Set<GeneroSummaryResponse> generos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
