package br.com.avaliacao.apimusicmanagement.domain.model.entity;

import br.com.avaliacao.apimusicmanagement.shared.persistence.auditable.AuditableEntity;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = "album_capa",
        indexes = {
                @Index(name = "idx_album_capa_album", columnList = "album_id"),
                @Index(name = "idx_album_capa_object_key", columnList = "object_key")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_album_capa_album_object", columnNames = {"album_id", "object_key"})
        }
)
public class AlbumCapa extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "album_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_album_capa_album")
    )
    private Album album;

    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "principal", nullable = false)
    private boolean principal = false;

    protected AlbumCapa() {

    }

    public AlbumCapa(String objectKey, String fileName, String contentType, Long sizeBytes) {
        this.objectKey = objectKey;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public Long getId() {
        return id;
    }

    public Album getAlbum() {
        return album;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AlbumCapa other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
