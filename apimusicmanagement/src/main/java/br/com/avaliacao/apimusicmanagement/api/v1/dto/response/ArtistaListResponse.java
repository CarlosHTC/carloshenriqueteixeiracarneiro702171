package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

public record ArtistaListResponse(
        Long id,
        String nome,
        String tipo,
        Long qtdAlbuns,
        String fotoUrl
) {

}
