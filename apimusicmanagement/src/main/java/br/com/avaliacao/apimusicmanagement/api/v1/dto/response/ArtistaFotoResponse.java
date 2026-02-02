package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

public record ArtistaFotoResponse(
        Long id,
        String contentType,
        Long sizeBytes,
        String url
) {
}
