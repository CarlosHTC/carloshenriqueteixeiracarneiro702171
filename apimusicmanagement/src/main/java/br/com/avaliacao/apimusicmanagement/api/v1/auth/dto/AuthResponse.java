package br.com.avaliacao.apimusicmanagement.api.v1.auth.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long accessExpiresInMs,
    long refreshExpiresInMs
) {

}
