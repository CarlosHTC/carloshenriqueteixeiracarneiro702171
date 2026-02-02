package br.com.avaliacao.apimusicmanagement.domain.model.entity;

import br.com.avaliacao.apimusicmanagement.domain.model.enums.TipoArtista;
import br.com.avaliacao.apimusicmanagement.shared.persistence.auditable.AuditableEntity;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(
        name = "artista",
        indexes = {
                @Index(name = "idx_artista_nome", columnList = "nome"),
                @Index(name = "idx_artista_tipo", columnList = "tio")
        }
)
public class Artista extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoArtista tipo;

    @OneToMany(mappedBy = "artista", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Album> albuns = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "artista_regional",
            joinColumns = @JoinColumn(name = "artista_id"),
            inverseJoinColumns = @JoinColumn(name = "regional_id")
    )
    private Set<Regional> regionais = new HashSet<>();

    protected Artista() {

    }

    public Artista(String nome, TipoArtista tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoArtista getTipo() {
        return tipo;
    }

    public List<Album> getAlbuns() {
        return albuns;
    }

    public Set<Regional> getRegionais() {
        return regionais;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setTipo(TipoArtista tipo) {
        this.tipo = tipo;
    }

    public void setRegionais(Set<Regional> regionais) {
        this.regionais = regionais;
    }

    public void adicionarAlbum(Album album) {
        albuns.add(album);
        album.setArtista(this);
    }

    public void removerAlbum(Album album) {
        albuns.remove(album);
        album.setArtista(null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Artista other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
