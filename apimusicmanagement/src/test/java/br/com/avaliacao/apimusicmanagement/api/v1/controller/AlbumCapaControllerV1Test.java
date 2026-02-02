package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.AlbumCapaResponse;
import br.com.avaliacao.apimusicmanagement.config.security.jwt.JwtAuthenticationFilter;
import br.com.avaliacao.apimusicmanagement.config.security.rateLimit.RateLimitFilter;
import br.com.avaliacao.apimusicmanagement.domain.service.AlbumCapaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlbumCapaControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
class AlbumCapaControllerV1Test {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AlbumCapaService albumCapaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    @Test
    @WithMockUser
    void upload_deveRetornar200() throws Exception {
        Long albumId = 10L;

        when(albumCapaService.upload(eq(albumId), anyList()))
                .thenReturn(List.of(new AlbumCapaResponse(1L, "capa.jpg", "image/jpeg", 3L, false, "http://x")));

        MockMultipartFile file = new MockMultipartFile(
                "files", "capa.jpg", "image/jpeg", "abc".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/albuns/{albumId}/capas", albumId)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void listar_deveRetornar200() throws Exception {
        Long albumId = 10L;

        when(albumCapaService.listar(albumId))
                .thenReturn(List.of(new AlbumCapaResponse(1L, "capa.jpg", "image/jpeg", 3L, false, "http://x")));

        mockMvc.perform(get("/api/v1/albuns/{albumId}/capas", albumId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void remover_deveRetornar204() throws Exception {
        Long albumId = 10L;
        Long capaId = 20L;

        mockMvc.perform(delete("/api/v1/albuns/{albumId}/capas/{capaId}", albumId, capaId))
                .andExpect(status().isNoContent());
    }
}
