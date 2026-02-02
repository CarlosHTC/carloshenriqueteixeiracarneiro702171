package br.com.avaliacao.apimusicmanagement.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String internalUrl;
    private String publicUrl;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String artistBucket;
    private Integer presignExpirationMinutes;

    public String getInternalUrl() {
        return internalUrl;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public String getArtistBucket() {
        return artistBucket;
    }

    public Integer getPresignExpirationMinutes() {
        return presignExpirationMinutes;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setArtistBucket(String artistBucket) {
        this.artistBucket = artistBucket;
    }

    public void setPresignExpirationMinutes(Integer presignExpirationMinutes) {
        this.presignExpirationMinutes = presignExpirationMinutes;
    }
}
