package br.com.avaliacao.apimusicmanagement.api.v1.mapper;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.ArtistaCreateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.ArtistaUpdateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.ArtistaResponse;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Artista;
import jakarta.validation.Valid;
import org.mapstruct.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ArtistaMapper {

    Artista toEntity(ArtistaCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@Valid ArtistaUpdateRequest request, @MappingTarget Artista entity);

    @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt())")
    @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt())")
    @Mapping(target = "regionalIds", expression = "java(mapRegionalIds(entity))")
    ArtistaResponse toResponse(Artista entity);

    default Set<Long> mapRegionalIds(Artista entity) {
        if (entity == null || entity.getRegionais() == null) {
            return Collections.emptySet();
        }
        return entity.getRegionais()
                .stream()
                .map(regional -> regional.getId())
                .collect(Collectors.toSet());
    }
}
