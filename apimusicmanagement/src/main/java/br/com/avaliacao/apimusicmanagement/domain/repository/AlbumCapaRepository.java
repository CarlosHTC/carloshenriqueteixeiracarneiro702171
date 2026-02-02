package br.com.avaliacao.apimusicmanagement.domain.repository;

import br.com.avaliacao.apimusicmanagement.domain.model.entity.AlbumCapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AlbumCapaRepository extends JpaRepository<AlbumCapa, Long> {

    List<AlbumCapa> findByAlbumIdOrderByCreatedAtDesc(Long albumId);

    Optional<AlbumCapa> findByIdAndAlbumId(Long id, Long albumId);

    List<AlbumCapa> findByAlbumIdInOrderByUpdatedAtDesc(Set<Long> albumIds);

    @Query(value = """
        SELECT DISTINCT ON (ac.album_id) ac.*
        FROM album_capa ac
        WHERE ac.album_id IN (:albumIds)
        ORDER BY ac.album_id, ac.updated_at DESC
        """, nativeQuery = true)
    List<AlbumCapa> findLatestByAlbumIds(@Param("albumIds") Set<Long> albumIds);
}
