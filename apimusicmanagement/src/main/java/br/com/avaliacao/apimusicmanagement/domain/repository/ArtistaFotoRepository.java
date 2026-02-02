package br.com.avaliacao.apimusicmanagement.domain.repository;

import br.com.avaliacao.apimusicmanagement.domain.model.entity.ArtistaFoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistaFotoRepository extends JpaRepository<ArtistaFoto, Long> {
    Optional<ArtistaFoto> findByArtistaId(Long artistaId);

    boolean existsByArtistaId(Long artistaId);

    void deleteByArtistaId(Long artistaId);
}
