package br.com.avaliacao.apimusicmanagement.domain.model.entity;

import br.com.avaliacao.apimusicmanagement.shared.persistence.auditable.AuditableEntity;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = "faixa",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_faixa_album_numero", columnNames = {"album_id", "numero"})
        },
        indexes = {
                @Index(name = "idx_faixa_album", columnList = "album_id"),
                @Index(name = "idx_faixa_titulo", columnList = "titulo")
        }
)
public class Faixa extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "album_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_faixa_album")
    )
    private Album album;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "duracao_segundos", nullable = false)
    private Integer duracaoSegundos;

    @Column(name = "explicita", nullable = false)
    private boolean explicita;

    protected Faixa() { }

    public Faixa(Integer numero, String titulo, Integer duracaoSegundos, boolean explicita) {
        this.numero = numero;
        this.titulo = titulo;
        this.duracaoSegundos = duracaoSegundos;
        this.explicita = explicita;
    }

    public Long getId() { return id; }
    public Album getAlbum() { return album; }
    public Integer getNumero() { return numero; }
    public String getTitulo() { return titulo; }
    public Integer getDuracaoSegundos() { return duracaoSegundos; }
    public boolean isExplicita() { return explicita; }

    public void setAlbum(Album album) { this.album = album; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDuracaoSegundos(Integer duracaoSegundos) { this.duracaoSegundos = duracaoSegundos; }
    public void setExplicita(boolean explicita) { this.explicita = explicita; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Faixa other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
