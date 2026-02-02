package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.mapper.ArtistaMapper;
import br.com.avaliacao.apimusicmanagement.config.security.jwt.JwtAuthenticationFilter;
import br.com.avaliacao.apimusicmanagement.config.security.rateLimit.RateLimitFilter;
import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.ArtistaListResponse;
import br.com.avaliacao.apimusicmanagement.domain.service.ArtistaService;
import br.com.avaliacao.apimusicmanagement.domain.service.ArtistaRegionalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArtistaControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
class ArtistaControllerV1Test {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ArtistaService artistaService;

    @MockitoBean
    ArtistaMapper artistaMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    @MockitoBean
    private ArtistaRegionalService artistaRegionalService;

    @Test
    @WithMockUser
    void listar_paginado_semFiltro() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        ArtistaListResponse artista = new ArtistaListResponse(1L, "Serj", "BANDA", 2L, "http://minio/serj");

        when(artistaService.listar(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(artista), pageable, 1));

        mockMvc.perform(get("/api/v1/artistas?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Serj"));

        verify(artistaService).listar(isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void listar_paginado_comFiltroNome() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(artistaService.listar(eq("mike"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/api/v1/artistas?nome=mike&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(artistaService).listar(eq("mike"), isNull(), isNull(), any(Pageable.class));
    }
}
