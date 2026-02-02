package br.com.avaliacao.apimusicmanagement.domain.repository.projection;

public interface ArtistaListProjection {
    Long getId();
    String getNome();
    String getTipo();
    Long getQtdAlbuns();

    String getFotoBucket();
    String getFotoObjectKey();
}
