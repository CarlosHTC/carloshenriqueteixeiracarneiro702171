package br.com.avaliacao.apimusicmanagement.domain.service;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.FaixaCreateRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.request.FaixaUpdateRequest;
import br.com.avaliacao.apimusicmanagement.domain.exception.ConflictException;
import br.com.avaliacao.apimusicmanagement.domain.exception.ResourceNotFoundException;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Faixa;
import br.com.avaliacao.apimusicmanagement.domain.repository.FaixaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FaixaService {

    private final FaixaRepository faixaRepository;
    private final AlbumService albumService;

    public FaixaService(FaixaRepository faixaRepository, AlbumService albumService) {
        this.faixaRepository = faixaRepository;
        this.albumService = albumService;
    }

    @Transactional(readOnly = true)
    public List<Faixa> listarPorAlbum(Long albumId) {
        albumService.buscarPorId(albumId);
        return faixaRepository.findByAlbumIdOrderByNumeroAsc(albumId);
    }

    @Transactional(readOnly = true)
    public Faixa buscarPorId(Long albumId, Long id) {
        return faixaRepository.findByIdAndAlbumId(id, albumId)
                .orElseThrow(() -> new ResourceNotFoundException(
                   "Faixa não encontrada: id=" + id + "(albumId=" + albumId + ")"));
    }

    @Transactional
    public Faixa criar(Long albumId, FaixaCreateRequest request) {
        var album = albumService.buscarPorId(albumId);

        if (faixaRepository.existsByAlbumIdAndNumero(albumId, request.numero())) {
            throw new ConflictException("Já existe uma faixa com numero=" + request.numero() + " no albumId=" + albumId);
        }


        Faixa faixa = new Faixa(request.numero(), request.titulo(), request.duracaoSegundos(), request.explicita());
        faixa.setAlbum(album);

        return faixaRepository.save(faixa);
    }

    @Transactional
    public Faixa atualizar(Long albumId, Long id, FaixaUpdateRequest request) {
        Faixa existente = buscarPorId(albumId, id);

        if (faixaRepository.existsByAlbumIdAndNumeroAndIdNot(albumId, request.numero(), id)) {
            throw new ConflictException("Já existe uma faixa com numero=" + request.numero() + " no albumId=" + albumId);
        }

        existente.setNumero(request.numero());
        existente.setTitulo(request.titulo());
        existente.setDuracaoSegundos(request.duracaoSegundos());
        existente.setExplicita(request.explicita());

        return faixaRepository.save(existente);
    }

    @Transactional
    public void deletar(Long albumId, Long id) {
        Faixa existente = buscarPorId(albumId, id);
        faixaRepository.delete(existente);
    }
}
