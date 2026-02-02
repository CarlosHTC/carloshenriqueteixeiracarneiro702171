package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

import br.com.avaliacao.apimusicmanagement.domain.model.enums.TipoArtista;

public record ArtistaSummaryResponse(
        Long id,
        String nome,
        TipoArtista tipo
) {

}
