package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumListResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.mapper.AlbumMapper;
import br.com.avaliacao.apimusicmanagement.config.security.jwt.JwtAuthenticationFilter;
import br.com.avaliacao.apimusicmanagement.config.security.rateLimit.RateLimitFilter;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Album;
import br.com.avaliacao.apimusicmanagement.domain.service.AlbumService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlbumControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
class AlbumControllerV1Test {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AlbumService albumService;

    @MockitoBean
    AlbumMapper albumMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    @Test
    @WithMockUser
    void listar_paginado_semFiltro() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        AlbumListResponse resp = new AlbumListResponse(
                1L, "Harakiri", null, null, null, null, null
        );

        when(albumService.listar(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(resp), pageable, 1));

        mvc.perform(get("/api/v1/albuns?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Harakiri"));

        verify(albumService).listar(isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void listar_paginado_comFiltroNome() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);

        when(albumService.listar(eq("post"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mvc.perform(get("/api/v1/albuns?nome=post&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(albumService).listar(eq("post"), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void buscarPorId_deveRetornar200() throws Exception {
        Album album = new Album("Black Album");

        when(albumService.buscarPorId(10L)).thenReturn(album);
        when(albumMapper.toResponse(album)).thenReturn(
                new AlbumResponse(10L, "Black Album", null, Set.of(), null, null)
        );

        mvc.perform(get("/api/v1/albuns/10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Black Album"));
    }

    @Test
    @WithMockUser
    void criar_deveRetornar201() throws Exception {
        Album salvo = new Album("Nevermind");
        when(albumService.criar("Nevermind", 1L, Set.of(10L, 11L))).thenReturn(salvo);
        when(albumMapper.toResponse(salvo)).thenReturn(
                new AlbumResponse(1L, "Nevermind", null, Set.of(), null, null)
        );

        mvc.perform(post("/api/v1/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Nevermind",
                                  "artistaId": 1,
                                  "generoIds": [10, 11]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Nevermind"));
    }
}
