package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.domain.exception.ResourceNotFoundException;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Genero;
import br.com.avaliacao.apimusicmanagement.domain.repository.GeneroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Service
public class GeneroService {

    private final GeneroRepository generoRepository;

    public GeneroService(GeneroRepository generoRepository) {
        this.generoRepository = generoRepository;
    }

    @Transactional(readOnly = true)
    public List<Genero> listar() {
        return generoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Genero buscarPorId(Long id) {
        return generoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gênero não encontrado: id=" + id));
    }

    @Transactional
    public Genero criar(Genero genero) {
        return generoRepository.save(genero);
    }

    @Transactional
    public Genero atualizar(Long id, Consumer<Genero> updater) {
        Genero existente = buscarPorId(id);
        updater.accept(existente);
        return generoRepository.save(existente);
    }

    @Transactional
    public void deletar(Long id) {
        Genero existente = buscarPorId(id);
        generoRepository.delete(existente);
    }

}