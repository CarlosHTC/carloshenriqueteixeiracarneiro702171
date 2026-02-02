package br.com.avaliacao.apimusicmanagement.domain.auth;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "auth_refresh_token")
public class AuthRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String jti;

    @Column(nullable = false, length = 120)
    private String username;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    protected AuthRefreshToken() {

    }

    public AuthRefreshToken(String jti, String username, Instant expiresAt) {
        this.jti = jti;
        this.username = username;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public Long getId() {
        return id;
    }

    public String getJti() {
        return jti;
    }

    public String getUsername() {
        return username;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void revoke() {
        this.revoked = true;
    }
}
