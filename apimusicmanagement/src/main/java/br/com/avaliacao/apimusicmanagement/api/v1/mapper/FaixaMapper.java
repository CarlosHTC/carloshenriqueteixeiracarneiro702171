package br.com.avaliacao.apimusicmanagement.api.v1.mapper;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.FaixaCreateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.FaixaUpdateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.FaixaResponse;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Faixa;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FaixaMapper {

    Faixa toEntity(FaixaCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(FaixaUpdateRequest request, @MappingTarget Faixa entity);

    @Mapping(target = "albumId", expression = "java(entity.getAlbum() != null ? entity.getAlbum().getId() : null)")
    @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt())")
    @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt())")
    FaixaResponse toResponse(Faixa entity);
}
