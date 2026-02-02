package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

import java.time.LocalDateTime;

public record FaixaResponse(
        Long id,
        Long albumId,
        Integer numero,
        String titulo,
        Integer duracaoSegundos,
        boolean explicita,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
