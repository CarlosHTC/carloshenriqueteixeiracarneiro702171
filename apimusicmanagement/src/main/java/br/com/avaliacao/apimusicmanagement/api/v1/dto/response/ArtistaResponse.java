package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

import br.com.avaliacao.apimusicmanagement.domain.model.enums.TipoArtista;

import java.time.LocalDateTime;
import java.util.Set;

public record ArtistaResponse(
        Long id,
        String nome,
        TipoArtista tipo,
        Set<Long> regionalIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
