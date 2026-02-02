package br.com.avaliacao.apimusicmanagement.domain.repository;

import br.com.avaliacao.apimusicmanagement.domain.model.entity.Regional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RegionalRepository extends JpaRepository<Regional, Long> {
    List<Regional> findByAtivoTrue();

    Optional<Regional> findFirstByExternalIdAndAtivoTrue(Long externalId);

    @Modifying
    @Query("update Regional r set r.ativo=false, r.inativadoEm=:now where r.id=:id and r.ativo=true")
    int inativar(@Param("id") Long id, @Param("now") Instant now);

    List<Regional> findByAtivo(Boolean ativo);
}
