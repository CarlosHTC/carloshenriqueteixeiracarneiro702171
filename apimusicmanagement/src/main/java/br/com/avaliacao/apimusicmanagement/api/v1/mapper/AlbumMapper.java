package br.com.avaliacao.apimusicmanagement.api.v1.mapper;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.*;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Artista;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Genero;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    default ArtistaSummaryResponse toArtistaSummary(Artista artista) {
        if (artista == null) return null;
        return new ArtistaSummaryResponse(artista.getId(), artista.getNome(), artista.getTipo());
    }

    default Set<GeneroSummaryResponse> toGeneroSummaries(Set<Genero> generos) {
        if (generos == null) return Set.of();
        return generos.stream()
                .map(genero -> new GeneroSummaryResponse(genero.getId(), genero.getNome()))
                .collect(Collectors.toSet());
    }

    @Mapping(target = "artista", expression = "java(toArtistaSummary(entity.getArtista()))")
    @Mapping(target = "generos", expression = "java(toGeneroSummaries(entity.getGeneros()))")
    @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt())")
    @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt())")
    AlbumResponse toResponse(Album entity);

    default AlbumListResponse toListResponse(Album entity, AlbumCapaPrincipalResponse capaPrincipal) {
        if (entity == null) return null;
        return new AlbumListResponse(
                entity.getId(),
                entity.getNome(),
                toArtistaSummary(entity.getArtista()),
                toGeneroSummaries(entity.getGeneros()),
                capaPrincipal,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
