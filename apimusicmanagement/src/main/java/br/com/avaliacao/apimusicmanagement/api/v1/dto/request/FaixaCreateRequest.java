package br.com.avaliacao.apimusicmanagement.api.v1.dto.request;

import jakarta.validation.constraints.*;

import java.util.Set;

public record FaixaCreateRequest(
        @NotNull @Min(1) @Max(999) Integer numero,
        @NotBlank @Size(max = 200) String titulo,
        @NotNull @Min(1) Integer duracaoSegundos,
        @NotNull Boolean explicita
) {

}
