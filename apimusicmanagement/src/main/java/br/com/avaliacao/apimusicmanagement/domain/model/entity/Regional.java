package br.com.avaliacao.apimusicmanagement.domain.model.entity;

import br.com.avaliacao.apimusicmanagement.shared.persistence.auditable.AuditableEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "regional")
public class Regional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private Long externalId;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "inativado_em")
    private Instant inativadoEm;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (ativo == null) ativo = true;
    }

    public static Regional novaAtiva(Long externalId, String nome, Instant now) {
        Regional r = new Regional();
        r.externalId = externalId;
        r.nome = nome;
        r.ativo = true;
        r.createdAt = now != null ? now : Instant.now();
        r.inativadoEm = null;
        return r;
    }

    public Long getId() {
        return id;
    }

    public Long getExternalId() {
        return externalId;
    }

    public String getNome() {
        return nome;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getInativadoEm() {
        return inativadoEm;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setInativadoEm(Instant inativadoEm) {
        this.inativadoEm = inativadoEm;
    }
}
