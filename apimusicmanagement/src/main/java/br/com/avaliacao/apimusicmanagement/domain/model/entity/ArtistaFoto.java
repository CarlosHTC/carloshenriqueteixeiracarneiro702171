package br.com.avaliacao.apimusicmanagement.domain.model.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "artista_foto")
public class ArtistaFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artista_id", nullable = false, unique = true)
    private Artista artista;

    @Column(nullable = false, length = 100)
    private String bucket;

    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePesist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Artista getArtista() {
        return artista;
    }

    public String getBucket() {
        return bucket;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setArtista(Artista artista) {
        this.artista = artista;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
}
