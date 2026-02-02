package br.com.avaliacao.apimusicmanagement.domain.repository;

import br.com.avaliacao.apimusicmanagement.domain.model.entity.Artista;
import br.com.avaliacao.apimusicmanagement.domain.model.enums.TipoArtista;
import br.com.avaliacao.apimusicmanagement.domain.repository.projection.ArtistaListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtistaRepository extends JpaRepository<Artista, Long>, JpaSpecificationExecutor<Artista> {
    Page<Artista> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @EntityGraph(attributePaths = "regionais")
    Optional<Artista> findById(Long id);

    @Query("""
              select
                a.id as id,
                a.nome as nome,
                a.tipo as tipo,
                count(distinct al.id) as qtdAlbuns,
                af.bucket as fotoBucket,
                af.objectKey as fotoObjectKey
              from Artista a
                left join a.albuns al
                left join ArtistaFoto af on af.artista.id = a.id
              group by a.id, a.nome, a.tipo, af.bucket, af.objectKey
            """)
    Page<ArtistaListProjection> listarPaginado(Pageable pageable);

    @Query("""
              select
                a.id as id,
                a.nome as nome,
                a.tipo as tipo,
                count(distinct al.id) as qtdAlbuns,
                af.bucket as fotoBucket,
                af.objectKey as fotoObjectKey
              from Artista a
                left join a.albuns al
                left join ArtistaFoto af on af.artista.id = a.id
              where :tipo = a.tipo
              group by a.id, a.nome, a.tipo, af.bucket, af.objectKey
            """)
    Page<ArtistaListProjection> findByTipo(@Param("tipo")TipoArtista tipo, Pageable pageable);

    @Query("""
              select
                a.id as id,
                a.nome as nome,
                a.tipo as tipo,
                count(distinct al.id) as qtdAlbuns,
                af.bucket as fotoBucket,
                af.objectKey as fotoObjectKey
              from Artista a
                left join a.albuns al
                left join ArtistaFoto af on af.artista.id = a.id
              where (:nome is null or lower(a.nome) like lower(concat('%', :nome, '%')))
              group by a.id, a.nome, a.tipo, af.bucket, af.objectKey
            """)
    Page<ArtistaListProjection> listarPaginadoComNome(@Param("nome") String nome, Pageable pageable);

    @Query(
            value = """
              select
                a.id as id,
                a.nome as nome,
                a.tipo as tipo,
                count(distinct al.id) as qtdAlbuns,
                af.bucket as fotoBucket,
                af.objectKey as fotoObjectKey
              from Artista a
                left join a.albuns al
                left join ArtistaFoto af on af.artista.id = a.id
              where
                (:tipo is null or a.tipo = :tipo)
                and (
                  coalesce(:nome, '') = ''
                  or lower(a.nome) like lower(concat('%', coalesce(:nome, ''), '%'))
                )
                and (
                  :regionalId is null
                  or exists (
                    select 1
                    from a.regionais r
                    where r.id = :regionalId and r.ativo = true
                  )
                  or not exists (select 1 from a.regionais r2)
                )
              group by a.id, a.nome, a.tipo, af.bucket, af.objectKey
            """,
            countQuery = """
              select count(distinct a.id)
              from Artista a
                left join a.albuns al
                left join ArtistaFoto af on af.artista.id = a.id
              where
                (:tipo is null or a.tipo = :tipo)
                and (
                  coalesce(:nome, '') = ''
                  or lower(a.nome) like lower(concat('%', coalesce(:nome, ''), '%'))
                )
                and (
                  :regionalId is null
                  or exists (
                    select 1
                    from a.regionais r
                    where r.id = :regionalId and r.ativo = true
                  )
                  or not exists (select 1 from a.regionais r2)
                )
            """
    )
    Page<ArtistaListProjection> listarPaginadoFiltrado(
            @Param("tipo") TipoArtista tipo,
            @Param("nome") String nome,
            @Param("regionalId") Long regionalId,
            Pageable pageable
    );
}
