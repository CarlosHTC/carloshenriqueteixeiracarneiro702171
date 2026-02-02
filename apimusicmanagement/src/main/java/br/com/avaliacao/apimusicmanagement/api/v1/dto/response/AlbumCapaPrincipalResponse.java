package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

public record AlbumCapaPrincipalResponse(
    Long id,
    String fileName,
    String conectType,
    Long sizeBytes,
    String url
) {

}
