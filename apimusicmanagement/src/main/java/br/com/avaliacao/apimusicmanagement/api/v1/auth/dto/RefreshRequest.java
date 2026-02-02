package br.com.avaliacao.apimusicmanagement.api.v1.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank String refreshToken
) {

}
