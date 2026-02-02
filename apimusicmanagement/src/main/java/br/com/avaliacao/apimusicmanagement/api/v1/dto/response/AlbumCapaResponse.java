package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

public record AlbumCapaResponse(
    Long id,
    String fileName,
    String contentType,
    Long sizeBytes,
    boolean principal,
    String url
) {

}
