package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;

import java.time.LocalDateTime;

public record GeneroResponse(
        Long id,
        String nome,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
