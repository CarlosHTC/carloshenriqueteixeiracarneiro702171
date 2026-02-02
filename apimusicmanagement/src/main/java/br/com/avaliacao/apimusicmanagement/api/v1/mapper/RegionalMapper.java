package br.com.avaliacao.apimusicmanagement.api.v1.mapper;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.RegionalResponse;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Regional;

public final class RegionalMapper {

    private RegionalMapper() {

    }

    public static RegionalResponse toResponse(Regional regional) {
        return new RegionalResponse(
                regional.getId(),
                regional.getExternalId(),
                regional.getNome(),
                regional.getAtivo()
        );
    }
}
