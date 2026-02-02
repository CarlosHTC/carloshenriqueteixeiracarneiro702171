package br.com.avaliacao.apimusicmanagement.domain.repository;

import br.com.avaliacao.apimusicmanagement.domain.model.entity.Faixa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FaixaRepository extends JpaRepository<Faixa, Long> {

    List<Faixa> findByAlbumIdOrderByNumeroAsc(Long albumId);

    Optional<Faixa> findByIdAndAlbumId(Long id, Long albumId);

    boolean existsByAlbumIdAndNumero(Long albumId, Integer numero);

    boolean existsByAlbumIdAndNumeroAndIdNot(Long albumId, Integer numero, Long id);
}
