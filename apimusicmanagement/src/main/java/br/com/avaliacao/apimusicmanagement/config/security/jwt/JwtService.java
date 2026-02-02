package br.com.avaliacao.apimusicmanagement.config.security.jwt;

import br.com.avaliacao.apimusicmanagement.config.security.AppSecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final AppSecurityProperties securityProperties;
    private final SecretKey secretKey;

    public JwtService(AppSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(securityProperties.getJwt().getSecret()));
    }

    public String generateAccessToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(securityProperties.getJwt().getAccessExpirationMs())))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public RefreshTokenBundle generateRefreshToken(String subject) {
        String jti = UUID.randomUUID().toString().replace("-", "");
        Date exp = Date.from(Instant.now().plusMillis(securityProperties.getJwt().getRefreshExpirationMs()));

        String token = Jwts.builder()
                .subject(subject)
                .id(jti)
                .issuedAt(new Date())
                .expiration(exp)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        return new RefreshTokenBundle(token, jti, exp.toInstant());
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public String getSubject(String token) {
        return parse(token).getPayload().getSubject();
    }

    public String getJti(String token) {
        return parse(token).getPayload().getId();
    }

    public static class RefreshTokenBundle {
        public final String token;
        public final String jti;
        public final Instant expiresAt;

        public RefreshTokenBundle(String token, String jti, Instant expiresAt) {
            this.token = token;
            this.jti = jti;
            this.expiresAt = expiresAt;
        }
    }
}
