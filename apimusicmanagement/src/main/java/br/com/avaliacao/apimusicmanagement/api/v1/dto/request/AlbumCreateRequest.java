package br.com.avaliacao.apimusicmanagement.api.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AlbumCreateRequest(
    @NotBlank @Size(max = 200) String nome,
    @NotNull Long artistaId,
    Set<Long> generoIds
) {

}
