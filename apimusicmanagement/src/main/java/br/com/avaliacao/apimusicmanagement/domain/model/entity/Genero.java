package br.com.avaliacao.apimusicmanagement.domain.model.entity;

import br.com.avaliacao.apimusicmanagement.shared.persistence.auditable.AuditableEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
        name = "genero",
        uniqueConstraints = @UniqueConstraint(name = "uk_genero_nome", columnNames = "nome"),
        indexes = @Index(name = "idx_genero_nome", columnList = "nome")
)
public class Genero extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @ManyToMany(mappedBy = "generos")
    private Set<Album> albuns = new HashSet<>();

    protected Genero() { }

    public Genero(String nome) {
        this.nome = nome;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Set<Album> getAlbuns() { return albuns; }

    public void setNome(String nome) { this.nome = nome; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genero other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
