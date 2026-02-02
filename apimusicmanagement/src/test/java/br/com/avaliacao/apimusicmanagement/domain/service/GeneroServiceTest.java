package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.domain.model.entity.Genero;
import br.com.avaliacao.apimusicmanagement.domain.repository.GeneroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneroServiceTest {

    @Mock
    private GeneroRepository generoRepository;

    @InjectMocks
    private GeneroService generoService;

    @Test
    void listar_deveRetornarLista() {
        when(generoRepository.findAll()).thenReturn(List.of(new Genero("Rock")));

        var resposta = generoService.listar();

        assertEquals(1, resposta.size());
        verify(generoRepository).findAll();
    }
}
