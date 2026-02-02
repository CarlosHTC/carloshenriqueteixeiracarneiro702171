package br.com.avaliacao.apimusicmanagement.api.v1.mapper;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.GeneroCreateRquest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.GeneroUpdateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.GeneroResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.GeneroSummaryResponse;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Genero;
import jakarta.validation.Valid;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface GeneroMapper {

    Genero toEntity(GeneroCreateRquest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@Valid GeneroUpdateRequest request, @MappingTarget Genero entity);

    @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt())")
    @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt())")
    GeneroResponse toResponse(Genero entity);

    GeneroSummaryResponse toSummary(Genero entity);
}
