package br.com.avaliacao.apimusicmanagement.config.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.integrations.regionais")
public class RegionalIntegrationProperties {
    private String url;
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 5000;
    private boolean syncOnStartup = true;

    public String getUrl() {
        return url;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public boolean isSyncOnStartup() {
        return syncOnStartup;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public void setSyncOnStartup(boolean syncOnStartup) {
        this.syncOnStartup = syncOnStartup;
    }
}
