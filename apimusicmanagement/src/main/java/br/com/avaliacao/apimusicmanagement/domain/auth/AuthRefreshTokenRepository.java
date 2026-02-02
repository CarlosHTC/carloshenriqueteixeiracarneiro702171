package br.com.avaliacao.apimusicmanagement.domain.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface AuthRefreshTokenRepository extends JpaRepository<AuthRefreshToken, Long> {

    Optional<AuthRefreshToken> findByJti(String jti);

    long deleteByExpiresAtBefore(Instant instant);

}
