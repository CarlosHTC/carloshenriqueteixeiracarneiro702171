package br.com.avaliacao.apimusicmanagement.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private RateLimit ratelimit = new RateLimit();
    private Admin admin = new Admin();

    public Jwt getJwt() {
        return jwt;
    }
    public Cors getCors() {
        return cors;
    }
    public RateLimit getRatelimit() {
        return ratelimit;
    }
    public Admin getAdmin() {
        return admin;
    }

    public static class Jwt {
        private String secret;
        private long accessExpirationMs;
        private long refreshExpirationMs;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getAccessExpirationMs() { return accessExpirationMs; }
        public void setAccessExpirationMs(long accessExpirationMs) { this.accessExpirationMs = accessExpirationMs; }
        public long getRefreshExpirationMs() { return refreshExpirationMs; }
        public void setRefreshExpirationMs(long refreshExpirationMs) { this.refreshExpirationMs = refreshExpirationMs; }
    }

    public static class Cors {
        private List<String> allowedOrigins;

        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }

    public static class RateLimit {
        private int requestsPerMinute = 10;

        public int getRequestsPerMinute() { return requestsPerMinute; }
        public void setRequestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
    }

    public static class Admin {
        private String username;
        private String password;
        private List<String> roles;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
    }
}
