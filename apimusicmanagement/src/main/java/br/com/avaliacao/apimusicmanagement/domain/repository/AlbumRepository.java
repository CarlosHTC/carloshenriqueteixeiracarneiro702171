package br.com.avaliacao.apimusicmanagement.domain.repository;

import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    @EntityGraph(attributePaths = {"artista", "generos"})
    Page<Album> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @EntityGraph(attributePaths = {"artista", "generos"})
    Page<Album> findByArtistaId(Long artistaId, Pageable pageable);

    @EntityGraph(attributePaths = {"artista", "generos"})
    List<Album> findAll();

    @EntityGraph(attributePaths = {"artista", "generos"})
    Page<Album> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"artista", "generos"})
    Optional<Album> findById(Long id);
}
