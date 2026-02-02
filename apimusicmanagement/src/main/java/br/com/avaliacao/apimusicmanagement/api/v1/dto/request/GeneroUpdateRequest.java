package br.com.avaliacao.apimusicmanagement.api.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeneroUpdateRequest(
    @NotBlank @Size(max = 120) String nome
) {

}
