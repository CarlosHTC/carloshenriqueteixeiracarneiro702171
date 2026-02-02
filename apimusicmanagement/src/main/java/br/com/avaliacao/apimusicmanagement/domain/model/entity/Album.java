package br.com.avaliacao.apimusicmanagement.domain.model.entity;

import br.com.avaliacao.apimusicmanagement.shared.persistence.auditable.AuditableEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
        name = "album",
        indexes = {
                @Index(name = "idx_album_nome", columnList = "nome"),
                @Index(name = "idx_album_artista", columnList = "artista_id")
        }
)
public class Album extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "artista_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_album_artista")
    )
    private Artista artista;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Faixa> faixas = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "album_genero",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id")
    )
    private Set<Genero> generos = new HashSet<>();

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlbumCapa> capas = new ArrayList<>();

    protected Album() {

    }

    public Album(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Artista getArtista() {
        return artista;
    }

    public List<Faixa> getFaixas() {
        return faixas;
    }

    public Set<Genero> getGeneros() {
        return generos;
    }

    public List<AlbumCapa> getCapas() {
        return capas;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setArtista(Artista artista) {
        this.artista = artista;
    }

    public void adicionarFaixa(Faixa faixa) {
        faixas.add(faixa);
        faixa.setAlbum(this);
    }

    public void removerFaixa(Faixa faixa) {
        faixas.remove(faixa);
        faixa.setAlbum(null);
    }

    public void adicionarGenero(Genero genero) {
        generos.add(genero);
        genero.getAlbuns().add(this);
    }

    public void removerGenero(Genero genero) {
        generos.remove(genero);
        genero.getAlbuns().remove(this);
    }

    public void adicionarCapa(AlbumCapa capa) {
        capas.add(capa);
        capa.setAlbum(this);
    }

    public void removerCapa(AlbumCapa capa) {
        capas.remove(capa);
        capa.setAlbum(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
