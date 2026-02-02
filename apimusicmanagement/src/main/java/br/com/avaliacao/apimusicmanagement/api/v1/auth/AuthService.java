package br.com.avaliacao.apimusicmanagement.api.v1.auth;

import br.com.avaliacao.apimusicmanagement.config.security.AppSecurityProperties;
import br.com.avaliacao.apimusicmanagement.config.security.jwt.JwtService;
import br.com.avaliacao.apimusicmanagement.domain.auth.AuthRefreshToken;
import br.com.avaliacao.apimusicmanagement.domain.auth.AuthRefreshTokenRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthRefreshTokenRepository refreshTokenRepository;
    private final AppSecurityProperties securityProperties;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, AuthRefreshTokenRepository refreshTokenRepository, AppSecurityProperties securityProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityProperties = securityProperties;
    }

    @Transactional
    public Tokens login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        String access = jwtService.generateAccessToken(username);
        var refreshBundle = jwtService.generateRefreshToken(username);

        refreshTokenRepository.save(new AuthRefreshToken(refreshBundle.jti, username, refreshBundle.expiresAt));

        return new Tokens(access, refreshBundle.token);
    }

    @Transactional
    public Tokens refresh(String refreshToken) {
        String username = jwtService.getSubject(refreshToken);
        String jti = jwtService.getJti(refreshToken);

        var stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expirado/revogado");
        }

        stored.revoke();

        String newAccess = jwtService.generateAccessToken(username);
        var newRefresh = jwtService.generateRefreshToken(username);
        refreshTokenRepository.save(new AuthRefreshToken(newRefresh.jti, username, newRefresh.expiresAt));

        return new Tokens(newAccess, newRefresh.token);
    }

    @Transactional
    public void logout(String refreshToken) {
        String jti = jwtService.getJti(refreshToken);
        refreshTokenRepository.findByJti(jti).ifPresent(AuthRefreshToken::revoke);
    }

    public long accessExpMs() {
        return securityProperties.getJwt().getAccessExpirationMs();
    }

    public long refreshExpMs() {
        return securityProperties.getJwt().getRefreshExpirationMs();
    }

    public record Tokens(
            String accesToken,
            String refreshToken) {

    }
}
