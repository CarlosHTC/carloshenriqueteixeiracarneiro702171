package br.com.avaliacao.apimusicmanagement.api.v1.dto.request;

import br.com.avaliacao.apimusicmanagement.domain.model.enums.TipoArtista;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ArtistaUpdateRequest(
    @NotBlank @Size(max = 200) String nome,
    @NotNull TipoArtista tipo,
    Set<Long> regionalIds
) {

}
