package br.com.avaliacao.apimusicmanagement.api.v1.dto.response;

public record RegionalResponse(
        Long id,
        Long externalId,
        String nome,
        Boolean ativo
) {

}
